package sg.vouch.vouchsdk.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.location.Location
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.StrictMode
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
    val eventCloseActivity = MutableLiveData<String>()
    val loadConfiguration = MutableLiveData<ConfigResponseModel>()
    val eventChangeStateToGreeting = MutableLiveData<Boolean>()
    val eventScroll = MutableLiveData<Void>()
    val eventOnSuccessAudio = MutableLiveData<Void>()
    val eventOnFailedAudio = MutableLiveData<Void>()
    var currentLocation = Pair(0.0, 0.0)

    val eventUpdateList = MutableLiveData<VouchChatUpdateEvent>()

    private var currentPage = 1

    var mMultipartImage: MultipartBody.Part? = null
    var mPathLocal: String = ""
    var mUriImage: Uri? = null

    var startUpdateSong = false
    @SuppressLint("UseSparseArrays")
    var audioSeek = HashMap<Int, Int>()
    var currentAudioMedia = VouchChatModel()
    var audioDuration = HashMap<String, Int>()

    private var retryCount = 0
    var mMediaPlayer: MediaPlayer? = null
    var isDataNew = false

    fun start() {
        mVouchCore = VouchCore()
        mVouchSDK.init(this@VouchChatViewModel)
    }

    override fun onRegistered() {
        getLayoutConfiguration()
    }

    private fun getLayoutConfiguration() {
        mVouchSDK.getConfig(object : GetConfigCallback {

            override fun onSuccess(data: ConfigResponseModel) {
                //testing warna
//
//                data.rightBubbleColor = null
//                data.rightBubbleBgColor = null
//                data.leftBubbleBgColor = null
//                data.leftBubbleColor = null

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

    /**
     * The method is used to call API chat
     * @param reset Boolean value ex. true, false
     */
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
            override fun onUnAuthorize() {
                retryRegisterUser()
            }

            override fun onSuccess(data: List<MessageResponseModel>) {
                if (data.isNotEmpty()) {
                    eventChangeStateToGreeting.value = false

                    data.apply {
                        if (isPaginating) {
                            forEach {
                                insertDataByFiltered(it, !reset, true)
                            }
                        } else {
                            asReversed().forEach {
                                insertDataByFiltered(it, !reset)
                            }
                        }
                        if (!firstOrNull()?.quickReplies.isNullOrEmpty() && reset) {
                            insertDataQuickReply(data.first())
                        }
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

    /**
     * The method is used to update list message
     * @param message MessageResponseModel
     */
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

    var saveWhenDisconnect :VouchChatModel? = null
    var saveWhenDisconnect2 : VouchChatModel? = null

    override fun onConnected() {
        changeConnectStatus.value = true

        if(!bDataChat.isEmpty() && saveWhenDisconnect2 != null && saveWhenDisconnect2!!.title.equals(bDataChat[0].title)){
            bDataChat.add(0, saveWhenDisconnect!!)
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
            saveWhenDisconnect2 = null
        }
    }

    override fun onDisconnected(isActionFromUser: Boolean) {
        changeConnectStatus.value = false
        if(!bDataChat.isEmpty() && bDataChat[0].type == VouchChatType.TYPE_QUICK_REPLY){
            saveWhenDisconnect = bDataChat[0]
            saveWhenDisconnect2 = bDataChat[1]
            removeDataChat(0)
        }
        if (!isActionFromUser) {
            retryConnectionHandler()
        }
    }

    override fun onError(message: String) {
        eventShowMessage.value = message
        if(message.toLowerCase().contains("password is not correct")){
            eventCloseActivity.value = message
        }
    }

    fun disconnectSocket() {
        mVouchSDK.disconnect()
    }

    fun reconnectSocket() {
        mVouchSDK.reconnect(this@VouchChatViewModel, forceReconnect = true)
    }

    /**
     * The method is used when success get current location
     * @param location Location
     */
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
    private fun insertDataByFiltered(message: MessageResponseModel, appendInLast: Boolean = false, isPaginating: Boolean = false) {
        if (message.senderId != null && bDataChat.any { it.isPendingMessage }) {
            updateUserMessage(bDataChat.indexOf(bDataChat.first { it.isPendingMessage }), message)
        } else {
            when (message.msgType) {
                "list" -> {
                    if (isPaginating) {
                        insertDataList(message, appendInLast, true)
                    }else{
                        insertDataList(message, appendInLast, false)
                    }
                }
                "gallery" -> {
                    insertDataGallery(message, appendInLast)
                }
                else -> {
                    if (isPaginating) {
                        insertDataButton(message.buttons?.asReversed() ?: emptyList(), message, appendInLast, true)
                    }
                    insertDataChat(listOf(message), appendInLast)
                }
            }
            if (!isPaginating) {
                insertDataButton(message.buttons ?: emptyList(), message, appendInLast)
            }
        }
    }
    /**
     * Insert GIF Typing
     * @param typing Boolean value ex. true, false
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
    /**
     * Insert Pending image
     */
    private fun insertPendingImage(imageUri: Uri? = null, path: String? = ""): VouchChatModel {
        val pendingDataImage = VouchChatModel("", "", true, VouchChatType.TYPE_IMAGE, "-", imageUri = imageUri, mediaUrl = path?:"", isPendingMessage = true, idSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
        bDataChat.add(0, pendingDataImage)
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        return pendingDataImage
    }

    private fun insertPendingVideo(videoUrl: String): VouchChatModel {
        val pendingDataVideo = VouchChatModel("", "", true, VouchChatType.TYPE_VIDEO, "-", mediaUrl = videoUrl, isPendingMessage = true, idSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
        bDataChat.add(0, pendingDataVideo)
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        return pendingDataVideo
    }

    /**
     * Insert Failed message
     */
    private fun insertFailedMessage(body: MessageBodyModel) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel(body.text.safe(), "", true, VouchChatType.TYPE_TEXT, "-", mediaUrl = "", isPendingMessage = true, isFailedMessage = true)
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    private fun insertFailedImage(msgType : String, body: MultipartBody.Part, imageUri : Uri, path: String) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_IMAGE, "-", imageUri = imageUri, mediaUrl = path, isPendingMessage = true, isFailedMessage = true, msgType = msgType, body = body, idSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }
    private fun insertFailedVideo(msgType : String, body: MultipartBody.Part, path : String) {
        removeDataChat(0)
        bDataChat.add(
            0,
            VouchChatModel("", "", true, VouchChatType.TYPE_VIDEO, "-", mediaUrl = path, isPendingMessage = true, isFailedMessage = true, msgType = msgType, body = body, idSent = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString())
        )
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }

    /**
     * Update sender message
     */
    private fun updateUserMessage(position: Int, data: MessageResponseModel, idSent: String = "") {
        val chat = when (data.msgType) {
            "text" -> VouchChatModel(
                data.text.safe(),
                "",
                true,
                VouchChatType.TYPE_TEXT,
                data.createdAt.safe(),
                mediaUrl = data.mediaUrl ?: ""
            )
            "image" -> {
                VouchChatModel(
                    "",
                    "",
                    true,
                    VouchChatType.TYPE_IMAGE,
                    data.createdAt.safe(),
                    imageUri = mUriImage,
                    mediaUrl = data.text.safe()
                )
            }
            "video" ->{
                val mMediaUrl: String = if(mPathLocal != "") {
                    mPathLocal
                } else {
                    data.text.safe()
                }

                VouchChatModel(
                    "",
                    "",
                    true,
                    VouchChatType.TYPE_VIDEO,
                    data.createdAt.safe(),
                    mediaUrl = mMediaUrl
                )
            }
            else -> VouchChatModel(
                data.text.safe(),
                "",
                true,
                VouchChatType.TYPE_TEXT,
                data.createdAt.safe(),
                mediaUrl = data.mediaUrl ?: ""
            )
        }

        if(data.msgType == "image" || data.msgType == "video"){
            if(bDataChat.any { it.idSent == idSent }){
                bDataChat.find { it.idSent == idSent }.let{
                    it?.isPendingMessage = false
                    it?.createdAt = chat.createdAt
                    it?.mediaUrl = chat.mediaUrl
                }
            } else {
                bDataChat[position] = chat
            }
        } else {
            bDataChat[position] = chat
        }

        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_FORCE_UPDATE, startPosition = position)
        mPathLocal = ""
        mMultipartImage = null
        mUriImage = null
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
                isHaveOutsideButton = (it.buttons ?: emptyList()).isNotEmpty(),
                mediaUrl = it.text.safe()
            )
        }

        if (appendInLast) {
            bDataChat.addAll(content)
            eventUpdateList.value =
                VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = bDataChat.size)
        } else {
            if (bDataChat.firstOrNull()?.type == VouchChatType.TYPE_QUICK_REPLY) {
                removeDataChat(0)
            }
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
                    isHaveOutsideButton = (data.buttons ?: emptyList()).isNotEmpty(),
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
                    isHaveOutsideButton = (data.buttons ?: emptyList()).isNotEmpty(),
                    quickReplies = data.quickReplies ?: emptyList()
                )
            )
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        }
    }

    /**
     * Add new chat title-list into list
     */
    private fun insertDataList(data: MessageResponseModel, appendInLast: Boolean = false, isPaginating: Boolean = false) {
        var datalist = data.lists
        if(isPaginating){
            datalist = datalist?.asReversed()
        }
        (datalist ?: emptyList()).forEachIndexed { pos, it ->
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
                        isFirstListContent = if (isPaginating) pos == data.lists!!.size - 1 else pos == 0,
                        isLastListContent = if (isPaginating) pos == 0 else pos == data.lists!!.size - 1
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
                        isFirstListContent = if (isPaginating) pos == data.lists!!.size - 1 else pos == 0,
                        isLastListContent = if (isPaginating) pos == 0 else pos == data.lists!!.size - 1
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
    private fun insertDataButton(data: List<ButtonModel>, dataParent: MessageResponseModel, appendInLast: Boolean = false, isPaginating: Boolean = false) {
        data.forEachIndexed { pos, it ->
            if (appendInLast) {
                bDataChat.add(
                    VouchChatModel(
                        title = it.title.safe(),
                        subTitle = "",
                        isMyChat = false,
                        type = VouchChatType.TYPE_BUTTON,
                        createdAt = dataParent.createdAt.safe(),
                        payload = it.payload ?: it.url.safe(),
                        typeValue = it.type.safe(),
                        isFirstListContent = if (isPaginating) pos == data.size - 1 else pos == 0,
                        isLastListContent = if (isPaginating) pos == 0 else pos == data.size - 1
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
                        createdAt = dataParent.createdAt.safe(),
                        payload = it.payload ?: it.url.safe(),
                        typeValue = it.type.safe(),
                        isFirstListContent = if (isPaginating) pos == data.size - 1 else pos == 0,
                        isLastListContent = if (isPaginating) pos == 0 else pos == data.size - 1
                    )
                )
                eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
            }

        }
    }

    /**
     * Remove chat
     * @param position Int value ex. 0, 1
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
    fun sendReplyMessage(body: MessageBodyModel, idSent: String = "") {
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
                        retryRegisterUser()
                    }

                    override fun onSuccess(data: MessageResponseModel) {
                        println("type image => $data")
                        val position = getDataPosition(idSent)
                        updateUserMessage(position, data, idSent)
                    }

                    override fun onError(message: String) {
                        eventShowMessage.value = message
                        mUriImage?.let { insertFailedImage("image", mMultipartImage ?: MultipartBody.Part.createFormData("", ""), it, body.text?:"") }
                    }
                })
            }
            "video" -> {
//                insertPendingVideo(body.text.safe())
                mVouchSDK.replyMessage(body, object : ReplyMessageCallback {
                    override fun onUnAuthorize() {
                        retryRegisterUser()
                    }

                    override fun onSuccess(data: MessageResponseModel) {
                        println("type video => $data")
                        val position = getDataPosition(idSent)
                        updateUserMessage(position, data, idSent)
                    }

                    override fun onError(message: String) {
                        eventShowMessage.value = message
                    }
                })
            }

        }
    }

    /**
     * Get data position
     */
    private fun getDataPosition(idSent: String): Int {
        return if(bDataChat.indexOfFirst { it.idSent == idSent} == -1){
            0
        } else {
            bDataChat.indexOfFirst{ it.idSent == idSent}
        }
    }

    /**
     * Retry to register
     */
    fun retryRegisterUser() {
        mVouchSDK.registerUser(object : RegisterCallback {
            override fun onSuccess(token: String, socketTicket: String) {
                mVouchCore.createSocket()

                var mMessageBodyModel = MessageBodyModel()

                if (mRepository.getLastMessage() != null) {
                    mMessageBodyModel = mRepository.getLastMessage()?:MessageBodyModel()
                }

                when {
                    mMessageBodyModel.msgType == "text" -> {
                        removeDataChat(0)
                        sendReplyMessage(mRepository.getLastMessage() ?: MessageBodyModel())
                    }
                    mMessageBodyModel.msgType == "image" -> sendImageMessage("image", mMultipartImage ?: MultipartBody.Part.createFormData("", ""), mPathLocal, mUriImage)
                    mMessageBodyModel.msgType == "video" -> sendImageMessage("video", mMultipartImage ?: MultipartBody.Part.createFormData("", ""), mPathLocal)
                }
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
                if(message.toLowerCase().contains("Failed to load conversation.")){
                    eventCloseActivity.value = message
                }
            }

        })
    }

    /**
     * Send image to server
     */
    fun sendImageMessage(msgType : String, body: MultipartBody.Part, path : String, imageUri: Uri? = null) {
        var dataChatTemp = VouchChatModel()

        if(msgType=="image") {
            dataChatTemp = insertPendingImage(imageUri, path)
        }else if(msgType=="video") {
            dataChatTemp = insertPendingVideo(path)
        }

        if (bDataChat != null && bDataChat[1].type == VouchChatType.TYPE_QUICK_REPLY) {
            removeDataChat(1)
        }

        saveDataToLocalTemp(dataChatTemp)

        sendImageMessageToServer(msgType, body, dataChatTemp)
    }

    private fun sendImageMessageToServer(msgType : String, body: MultipartBody.Part, dataChatTemp: VouchChatModel) {
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

                sendReplyMessage(message, dataChatTemp.idSent)
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
                if(msgType=="image") {
                    mUriImage?.let { insertFailedImage(msgType, body, it, dataChatTemp.mediaUrl) }
                }else if(msgType=="video") {
                    insertFailedVideo(msgType, body, dataChatTemp.mediaUrl)
                }
            }
        })
    }

    private fun saveDataToLocalTemp(data: VouchChatModel) {
        // save data to local
        mPathLocal = data.mediaUrl
        mMultipartImage = data.body
        mUriImage = data.imageUri
    }

    fun sendAudioMessage(body: SendAudioBodyModel) {
        mVouchSDK.sendAudio(body, object : AudioMessageCallback {
            override fun onUnAuthorize() {
                retryRegisterUser()
            }

            override fun onSuccess(data: SendAudioResponseModel) {
                eventOnSuccessAudio.value = null
            }

            override fun onError(message: String) {
                if(message.contains("500")){
                    eventShowMessage.value = "Failed to transcribe your voice"
                }else{
                    eventShowMessage.value = message
                }
                eventOnFailedAudio.value = null

            }
        })
    }
}