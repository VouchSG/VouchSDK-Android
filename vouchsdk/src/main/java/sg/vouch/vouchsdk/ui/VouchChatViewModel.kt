package sg.vouch.vouchsdk.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.location.Location
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.VouchCore
import sg.vouch.vouchsdk.VouchSDK
import sg.vouch.vouchsdk.callback.*
import sg.vouch.vouchsdk.data.model.config.response.ConfigResponseModel
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.data.model.message.response.ButtonModel
import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel
import sg.vouch.vouchsdk.data.model.message.response.SendAudioResponseModel
import sg.vouch.vouchsdk.data.model.message.response.UploadImageResponseModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.utils.Const.PAGE_SIZE
import sg.vouch.vouchsdk.utils.Injection
import sg.vouch.vouchsdk.utils.safe
import java.util.concurrent.TimeUnit

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatViewModel(application: Application) : AndroidViewModel(application), VouchCallback,
    OnSuccessListener<Location> {


    lateinit var mVouchSDK: VouchSDK
    private lateinit var mVouchCore: VouchCore

    private val mRepository = Injection.createRepository(application)

    var lastScrollPosition: Int = -1

    val bDataChat: MutableList<VouchChatModel> = mutableListOf()
    val isRequesting = MutableLiveData<Boolean>()

    var isPaginating: Boolean = false
    var isLastPage: Boolean = false
    var isTyping: Boolean = false

    val changeConnectStatus = MutableLiveData<Boolean>()
    val eventShowMessage = MutableLiveData<String>()
    val loadConfiguration = MutableLiveData<ConfigResponseModel>()
    val eventChangeStateToGreeting = MutableLiveData<Boolean>()
    val eventScroll = MutableLiveData<Void>()
    var currentLocation = Pair(0.0, 0.0)

    val eventUpdateList = MutableLiveData<VouchChatUpdateEvent>()

    private var currentPage = 1

    var mMultipartImage: MultipartBody.Part? = null

    var startUpdateSong = false
    var audioSeek = 0

    private var retryCount = 0

    fun start() {
        mVouchCore = VouchCore()
        mVouchSDK.init(this@VouchChatViewModel)
    }

    override fun onRegistered() {
        getLayoutConfiguration()
    }

    override fun onConnected() {
        changeConnectStatus.value = true
    }

    private fun getLayoutConfiguration() {
        mVouchSDK.getConfig(object : GetConfigCallback {

            override fun onSuccess(data: ConfigResponseModel) {
                loadConfiguration.value = data
                isRequesting.value = false
                if (bDataChat.isEmpty()) {
                    getChatContent(true)
                } else {
                    eventScroll.value = null
                }

            }

            override fun onError(message: String) {
                isRequesting.value = false
            }
        })
    }

    fun getChatContent(reset: Boolean = false) {
        isRequesting.value = true

        if (reset) {
            currentPage = 1
            bDataChat.clear()
            eventUpdateList.value = VouchChatUpdateEvent(VouchChatEnum.TYPE_FORCE_UPDATE, 0)
        } else {
            isPaginating = true
        }

        mVouchSDK.getListMessages(currentPage++, PAGE_SIZE, object : MessageCallback {
            override fun onSuccess(data: List<MessageResponseModel>) {
                if (data.isNotEmpty()) {
                    eventChangeStateToGreeting.value = false

                    data.asReversed().forEach {
                        insertDataByFiltered(it, !reset)
                    }

                    if (!data.firstOrNull()?.quickReplies.isNullOrEmpty() && reset) {
                        insertDataQuickReply(data.first())
                    }

                } else {
                    eventChangeStateToGreeting.value = true
                    bDataChat.clear()
                    insertDataChat(listOf(MessageResponseModel(text = loadConfiguration.value?.greetingMessage?.text.safe())))
                }

                isLastPage = data.size < PAGE_SIZE
                isPaginating = false
                isRequesting.value = false
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
                isRequesting.value = false
            }

        })
    }

    override fun onReceivedNewMessage(message: MessageResponseModel) {
        if(!bDataChat.isEmpty() && bDataChat[0].type == VouchChatType.TYPE_TYPING){
            removeDataChat(0)
        }
        Handler().postDelayed({
            eventChangeStateToGreeting.value = false
            insertDataByFiltered(message)
            if (!message.quickReplies.isNullOrEmpty()) {
                insertDataQuickReply(message)
            }
        }, 1000)
    }

    override fun onDisconnected(isActionFromUser: Boolean) {
        changeConnectStatus.value = false
        if (!isActionFromUser) {
            retryConnectionHandler()
        }
    }

    override fun onError(message: String) {
        eventShowMessage.value = message
    }

    fun disconnectSocket() {
        mVouchSDK.disconnect()
    }

    fun reconnectSocket() {
        mVouchSDK.reconnect(this@VouchChatViewModel, forceReconnect = true)
    }

    override fun onSuccess(location: Location?) {
        currentLocation = Pair(location?.latitude ?: 0.0, location?.longitude ?: 0.0)
        sendLocation()
    }

    /**
     * Retry connection function
     * */
    private fun retryConnectionHandler() {
        Thread {
            while (changeConnectStatus.value == false) {
                if (retryCount == 10) {
                    return@Thread
                } else {
                    retryCount += 1
                }
                reconnectSocket()
                TimeUnit.SECONDS.sleep(10)
            }
        }.start()
    }

    /**
     * General function for add
     */
    private fun insertDataByFiltered(message: MessageResponseModel, appendInLast: Boolean = false) {

        if (message.senderId != null && bDataChat.any { it.isPendingMessage }) {
            updateUserMessage(bDataChat.indexOf(bDataChat.first { it.isPendingMessage }), message)
        } else {
            when (message.msgType) {
                "list" -> {
                    insertDataList(message, appendInLast)
                }
                "gallery" -> {
                    insertDataGallery(message, appendInLast)
                }
                else -> {
                    insertDataChat(listOf(message), appendInLast)
                }
            }
            insertDataButton(message.buttons ?: emptyList(), appendInLast)
        }
    }
    /**
     * Insert GIF Typing
     */
    override fun onTyping(typing: Boolean) {
        if(typing != isTyping){
            isTyping = typing
            if(typing) {
                Handler().postDelayed({
                    bDataChat.add(
                        0,
                        VouchChatModel(
                            "",
                            "",
                            false,
                            VouchChatType.TYPE_TYPING,
                            "-",
                            mediaUrl = "",
                            isPendingMessage = true
                        )
                    )
                    eventUpdateList.value =
                        VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
                }, 1000)

             }
            else {
                if (!bDataChat.isEmpty() && bDataChat[0].type == VouchChatType.TYPE_TYPING) {
                    removeDataChat(0)
                }
            }
        }

    }
    /**
     * Insert Pending message
     */
    private fun insertPendingMessage(message: String) {
        bDataChat.add(
            0,
            VouchChatModel(message, "", true, VouchChatType.TYPE_TEXT, "-", mediaUrl = "", isPendingMessage = true)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    private fun insertFailedMessage(body: MessageBodyModel) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel(body.text.safe(), "", true, VouchChatType.TYPE_TEXT, "-", mediaUrl = "", isPendingMessage = true, isFailedMessage = true)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    private fun insertFailedImage(msgType : String, body: MultipartBody.Part, path : String) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_IMAGE, "-", mediaUrl = path, isPendingMessage = true, isFailedMessage = true, msgType = msgType, body = body)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    private fun insertFailedVideo(msgType : String, body: MultipartBody.Part, path : String) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_VIDEO, "-", mediaUrl = path, isPendingMessage = true, isFailedMessage = true, msgType = msgType, body = body)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    /**
     * Insert Pending image
     */
    private fun insertPendingImage(imageUrl: String) {
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_IMAGE, "-", mediaUrl = imageUrl, isPendingMessage = true)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }

    private fun insertPendingVideo(videoUrl: String) {
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_VIDEO, "-", mediaUrl = videoUrl, isPendingMessage = true)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }

    /**
     * Update sender message
     */
    private fun updateUserMessage(position: Int, data: MessageResponseModel) {
        val chat = when (data.msgType) {
            "text" -> VouchChatModel(
                data.text.safe(),
                "",
                true,
                VouchChatType.TYPE_TEXT,
                data.createdAt.safe(),
                mediaUrl = data.mediaUrl ?: ""
            )
            "image" -> VouchChatModel(
                "",
                "",
                true,
                VouchChatType.TYPE_IMAGE,
                data.createdAt.safe(),
                mediaUrl = data.text.safe()
            )
            "video" -> VouchChatModel(
                "",
                "",
                true,
                VouchChatType.TYPE_VIDEO,
                data.createdAt.safe(),
                mediaUrl = data.text.safe()
            )
            else -> VouchChatModel(
                data.text.safe(),
                "",
                true,
                VouchChatType.TYPE_TEXT,
                data.createdAt.safe(),
                mediaUrl = data.mediaUrl ?: ""
            )
        }
        bDataChat[position] = chat
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_UPDATE, startPosition = position)
    }

    /**
     * Add new chat into list
     */
    private fun insertDataChat(data: List<MessageResponseModel>, appendInLast: Boolean = false) {
        val content = data.map {
            val type = when (it.msgType) {
                "image" -> {
                    VouchChatType.TYPE_IMAGE
                }
                "audio" -> {
                    VouchChatType.TYPE_AUDIO
                }
                "video" -> {
                    VouchChatType.TYPE_VIDEO
                }
                else -> {
                    VouchChatType.TYPE_TEXT
                }
            }
            VouchChatModel(
                it.text.safe(),
                "",
                it.senderId != null,
                type,
                it.createdAt.safe(),
                mediaUrl = it.text.safe()
            )
        }

        if (appendInLast) {
            bDataChat.addAll(content)
            eventUpdateList.value =
                VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size)
        } else {
            bDataChat.addAll(0, content)
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        }
    }

    /**
     * Add new chat quickreply into list
     */
    private fun insertDataQuickReply(data: MessageResponseModel, appendInLast: Boolean = false) {
        if (appendInLast) {
            bDataChat.add(
                VouchChatModel(
                    title = data.text.safe(),
                    isMyChat = false,
                    type = VouchChatType.TYPE_QUICK_REPLY,
                    createdAt = data.createdAt.safe(),
                    quickReplies = data.quickReplies ?: emptyList()
                )
            )
            eventUpdateList.value =
                VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size - 1)
        } else {
            bDataChat.add(
                0,
                VouchChatModel(
                    title = data.text.safe(),
                    isMyChat = false,
                    type = VouchChatType.TYPE_QUICK_REPLY,
                    createdAt = data.createdAt.safe(),
                    quickReplies = data.quickReplies ?: emptyList()
                )
            )
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        }
    }

    /**
     * Add new chat title-list into list
     */
    private fun insertDataList(data: MessageResponseModel, appendInLast: Boolean = false) {
        (data.lists ?: emptyList()).forEachIndexed { pos, it ->
            var isMyChat = data.customerInfo == null
            if (appendInLast) {
                bDataChat.add(
                    VouchChatModel(
                        title = it.title.safe(),
                        subTitle = it.subtitle.safe(),
                        isMyChat = isMyChat,
                        type = VouchChatType.TYPE_LIST,
                        createdAt = data.createdAt.safe(),
                        mediaUrl = it.imageUrl.safe(),
                        buttonTitle = it.buttons?.firstOrNull()?.title.safe(),
                        payload = it.buttons?.firstOrNull()?.payload ?: it.buttons?.firstOrNull()?.url.safe(),
                        typeValue = it.buttons?.firstOrNull()?.type.safe(),
                        isHaveOutsideButton = (data.buttons ?: emptyList()).isNotEmpty(),
                        isFirstListContent = pos == 0,
                        isLastListContent = pos == (data.lists ?: emptyList()).size - 1
                    )
                )
                eventUpdateList.value =
                    VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size - 1)
            } else {
                bDataChat.add(
                    0,
                    VouchChatModel(
                        title = it.title.safe(),
                        subTitle = it.subtitle.safe(),
                        isMyChat = isMyChat,
                        type = VouchChatType.TYPE_LIST,
                        createdAt = data.createdAt.safe(),
                        mediaUrl = it.imageUrl.safe(),
                        buttonTitle = it.buttons?.firstOrNull()?.title.safe(),
                        payload = it.buttons?.firstOrNull()?.payload ?: it.buttons?.firstOrNull()?.url.safe(),
                        typeValue = it.buttons?.firstOrNull()?.type.safe(),
                        isHaveOutsideButton = (data.buttons ?: emptyList()).isNotEmpty(),
                        isFirstListContent = pos == 0,
                        isLastListContent = pos == (data.lists ?: emptyList()).size - 1
                    )
                )
                eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
            }
        }
    }

    /**
     * Add new chat gallery into list
     */
    private fun insertDataGallery(data: MessageResponseModel, appendInLast: Boolean = false) {
        var isMyChat = data.customerInfo == null
        if (appendInLast) {
            bDataChat.add(
                VouchChatModel(
                    isMyChat = isMyChat,
                    type = VouchChatType.TYPE_GALLERY,
                    createdAt = data.createdAt.safe(),
                    galleryElements = data.elements ?: emptyList()
                )
            )
            eventUpdateList.value =
                VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size - 1)
        } else {
            bDataChat.add(
                0,
                VouchChatModel(
                    isMyChat = isMyChat,
                    type = VouchChatType.TYPE_GALLERY,
                    createdAt = data.createdAt.safe(),
                    galleryElements = data.elements ?: emptyList()
                )
            )
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        }
    }

    /**
     * Add new chat button into list
     */
    private fun insertDataButton(data: List<ButtonModel>, appendInLast: Boolean = false) {
        data.forEachIndexed { pos, it ->
            if (appendInLast) {
                bDataChat.add(
                    VouchChatModel(
                        title = it.title.safe(),
                        subTitle = "",
                        isMyChat = false,
                        type = VouchChatType.TYPE_BUTTON,
                        payload = it.payload ?: it.url.safe(),
                        typeValue = it.type.safe(),
                        isFirstListContent = pos == 0,
                        isLastListContent = pos == data.size - 1
                    )
                )
                eventUpdateList.value =
                    VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size - 1)
            } else {
                bDataChat.add(
                    0,
                    VouchChatModel(
                        title = it.title.safe(),
                        subTitle = "",
                        isMyChat = false,
                        type = VouchChatType.TYPE_BUTTON,
                        payload = it.payload ?: it.url.safe(),
                        typeValue = it.type.safe(),
                        isFirstListContent = pos == 0,
                        isLastListContent = pos == data.size - 1
                    )
                )
                eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
            }

        }
    }

    private fun updateDataChat(data: List<MessageResponseModel>, position: Int, endPosition: Int? = null) {
        bDataChat.addAll(data.map {
            VouchChatModel(
                title = it.text.safe(),
                isMyChat = false,
                type = VouchChatType.TYPE_TEXT,
                createdAt = it.createdAt.safe()
            )
        })
        eventUpdateList.value =
            VouchChatUpdateEvent(type = VouchChatEnum.TYPE_UPDATE, startPosition = position, endPosition = endPosition)
    }

    /**
     * Remove chat
     */
    fun removeDataChat(position: Int, endPosition: Int? = null) {
        if (position == -1) {
            return
        }
        if (position != endPosition) {
            for (i in position..(endPosition ?: position)) {
                bDataChat.removeAt(position)
            }
        } else {
            bDataChat.removeAt(position)
        }
        eventUpdateList.value =
            VouchChatUpdateEvent(type = VouchChatEnum.TYPE_REMOVE, startPosition = position, endPosition = endPosition)
    }

    fun sendReference(text : TextView, loading : ProgressBar) {
        removeDataChat(bDataChat.size - 1)
        mVouchSDK.referenceSend("welcome", object : ReferenceSendCallback {
            override fun onSuccess() = Unit
            override fun onError(message: String){
                text.setText("Ok Lets Go!")
                text.isEnabled = true
                loading.visibility = View.GONE
            }
        })
    }

    fun sendLocation() {
        if (bDataChat.firstOrNull()?.type == VouchChatType.TYPE_QUICK_REPLY) {
            removeDataChat(0)
        }

        insertPendingMessage("Getting Location")
        Handler().postDelayed({
            mVouchSDK.sendLocation(
                LocationBodyModel(currentLocation.first, currentLocation.second),
                object : LocationMessageCallback {
                    override fun onSuccess() {
                        removeDataChat(0)
                    }

                    override fun onError(message: String) {

                    }

                })
        }, 800)
    }

    /**
     * Send message
     */
    fun sendReplyMessage(body: MessageBodyModel) {
        if (bDataChat.firstOrNull()?.type == VouchChatType.TYPE_QUICK_REPLY) {
            removeDataChat(0)
        }

        when (body.msgType) {
            "text" -> {
                insertPendingMessage(body.text.safe())

                mVouchSDK.replyMessage(body, object : ReplyMessageCallback {
                    override fun onUnAuthorize() {
                        mMultipartImage = null
                        retryRegisterUser()
                    }

                    override fun onSuccess(data: MessageResponseModel) {
                        println("type text => $data")
//                        updateUserMessage(0, data)
                    }

                    override fun onError(message: String) {
                        eventShowMessage.value = message
                        insertFailedMessage(body)
                    }
                })
            }

            "image" -> {
//                insertPendingImage(body.text.safe())
                mVouchSDK.replyMessage(body, object : ReplyMessageCallback {
                    override fun onUnAuthorize() {
                        mMultipartImage = null
                        retryRegisterUser()
                    }

                    override fun onSuccess(data: MessageResponseModel) {
                        println("type image => $data")
                        updateUserMessage(0, data)
                    }

                    override fun onError(message: String) {
                        eventShowMessage.value = message
//                        insertFailedImage(body)
                    }
                })
            }
            "video" -> {
//                insertPendingVideo(body.text.safe())
                mVouchSDK.replyMessage(body, object : ReplyMessageCallback {
                    override fun onUnAuthorize() {
                        mMultipartImage = null
                        retryRegisterUser()
                    }

                    override fun onSuccess(data: MessageResponseModel) {
                        println("type video => $data")
                        updateUserMessage(0, data)
                    }

                    override fun onError(message: String) {
                        eventShowMessage.value = message
                    }
                })
            }

        }
    }

    /**
     * Retry to register
     */
    fun retryRegisterUser() {
        mVouchSDK.registerUser(object : RegisterCallback {
            override fun onSuccess(token: String, socketTicket: String) {
                mVouchCore.createSocket()
                if (mMultipartImage == null) {
                    removeDataChat(0)
                    sendReplyMessage(mRepository.getLastMessage() ?: MessageBodyModel())
                } else {
                    sendImageMessage("image", mMultipartImage ?: MultipartBody.Part.createFormData("", ""), "")
                }
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
            }

        })
    }

    /**
     * Send image to server
     */
    fun sendImageMessage(msgType : String, body: MultipartBody.Part, path : String) {
        if(msgType=="image") {
            insertPendingImage(path)
        }else if(msgType=="video") {
            insertPendingVideo(path)
        }
        mMultipartImage = body
        mVouchSDK.sendImage(body, object : ImageMessageCallback {
            override fun onUnAuthorize() {
                retryRegisterUser()
            }

            override fun onSuccess(data: UploadImageResponseModel) {
                val message = MessageBodyModel(
                    msgType = msgType,
                    text = data.url,
                    type = msgType
                )

                sendReplyMessage(message)
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
                if(msgType=="image") {
                    insertFailedImage(msgType, body, path)
                }else if(msgType=="video") {
                    insertFailedVideo(msgType, body, path)
                }
            }
        })
    }

    fun sendAudioMessage(body: SendAudioBodyModel) {
        mVouchSDK.sendAudio(body, object : AudioMessageCallback {
            override fun onUnAuthorize() {
                retryRegisterUser()
            }

            override fun onSuccess(data: SendAudioResponseModel) {
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
            }
        })
    }
}