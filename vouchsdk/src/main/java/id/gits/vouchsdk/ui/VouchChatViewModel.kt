package id.gits.vouchsdk.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.callback.*
import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.ButtonModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.ui.model.VouchChatModel
import id.gits.vouchsdk.ui.model.VouchChatType
import id.gits.vouchsdk.utils.Const.PAGE_SIZE
import id.gits.vouchsdk.utils.safe

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatViewModel constructor(application: Application, username: String, password: String) :
    AndroidViewModel(application), VouchCallback {

    private val mVouchSDK: VouchSDK = VouchSDK.setCredential(username, password).createSDK(application)

    val bDataChat: MutableList<VouchChatModel> = mutableListOf()
    val isRequesting = MutableLiveData<Boolean>()
    val changeConnectStatus = MutableLiveData<Boolean>()
    val eventShowMessage = MutableLiveData<String>()
    val loadConfiguration = MutableLiveData<ConfigResponseModel>()
    val eventChangeStateToGreeting = MutableLiveData<Boolean>()

    val eventUpdateList = MutableLiveData<VouchChatUpdateEvent>()

    private var currentPage = 1


    fun start() {
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
                getChatContent(true)
            }

            override fun onError(message: String) {
                isRequesting.value = false
            }
        })
    }

    private fun getChatContent(reset: Boolean = false) {
        isRequesting.value = true

        if (reset) {
            currentPage = 1
            bDataChat.clear()
            eventUpdateList.value = VouchChatUpdateEvent(VouchChatEnum.TYPE_FORCE_UPDATE, 0)
        }

        mVouchSDK.getListMessages(currentPage, PAGE_SIZE, object : MessageCallback {
            override fun onSuccess(data: List<MessageResponseModel>) {
                if (data.isNotEmpty()) {
                    eventChangeStateToGreeting.value = false

                    data.asReversed().forEach {
                        insertDataChat(listOf(it))
                        if ((it.buttons?: emptyList()).isNotEmpty()) {
                            insertDataButton(it.buttons?: emptyList())
                        }
                    }

                    if (currentPage == 1 && data.firstOrNull()?.quickReplies?.isNotEmpty() == true) {
                        insertDataQuickReply(data.first())
                    }

                } else {
                    eventChangeStateToGreeting.value = true
                    bDataChat.clear()
                    insertDataChat(listOf(MessageResponseModel(text = loadConfiguration.value?.greetingMessage?.text.safe())))
                }
                isRequesting.value = false
            }

            override fun onError(message: String) {
                eventShowMessage.value = message
                isRequesting.value = false
            }

        })
    }

    override fun onReceivedNewMessage(message: MessageResponseModel) {
        eventChangeStateToGreeting.value = false
        insertDataChat(listOf(message))

        insertDataButton(message.buttons?: emptyList())

        if (!message.quickReplies.isNullOrEmpty()) {
            insertDataQuickReply(message)
        }
    }

    override fun onDisconnected(isActionFromUser: Boolean) {
        changeConnectStatus.value = false
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


    /**
     * Add new chat into list
     */
    private fun insertDataChat(data: List<MessageResponseModel>) {
        bDataChat.addAll(0, data.map {
            val type = when (it.msgType) {
                "image" -> {
                     VouchChatType.TYPE_IMAGE
                }
                "video" -> {
                    VouchChatType.TYPE_VIDEO
                }
                else -> {
                    VouchChatType.TYPE_TEXT
                }
            }
            VouchChatModel(it.text.safe(), it.senderId != null, type, it.createdAt.safe(), mediaUrl = it.mediaUrl?:"")
        })
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }


    /**
     * Add new chat quickreply into list
     */
    private fun insertDataQuickReply(data: MessageResponseModel) {
        bDataChat.add(0, VouchChatModel(data.text.safe(), false, VouchChatType.TYPE_QUICK_REPLY, data.createdAt.safe(), "", "", "",data.quickReplies ?: emptyList()))
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
    }


    /**
     * Add new chat button into list
     */
    private fun insertDataButton(data: List<ButtonModel>) {
        data.forEach {
            bDataChat.add(0, VouchChatModel(it.title.safe(), false, VouchChatType.TYPE_BUTTON, "", payload = it.payload?:it.url.safe(), typeValue = it.type.safe()))
            eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_INSERTED, startPosition = 0)
        }
    }

    private fun updateDataChat(data: List<MessageResponseModel>, position: Int, endPosition: Int? = null) {
        bDataChat.addAll(data.map {
            VouchChatModel(
                it.text.safe(),
                false,
                VouchChatType.TYPE_TEXT,
                it.createdAt.safe()
            )
        })
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_UPDATE, startPosition = position, endPosition = endPosition)
    }

    fun removeDataChat(position: Int, endPosition: Int? = null) {
        if(position != endPosition){
            for (i in position..(endPosition ?: position)) {
                bDataChat.removeAt(position)
            }
        } else {
            bDataChat.removeAt(position)
        }
        eventUpdateList.value = VouchChatUpdateEvent(type = VouchChatEnum.TYPE_REMOVE, startPosition = position, endPosition = endPosition)
    }

    fun sendReference() {
        removeDataChat(bDataChat.size - 1)
        mVouchSDK.referenceSend("welcome", object : ReferenceSendCallback {
            override fun onSuccess() = Unit
            override fun onError(message: String) = Unit
        })
    }

    fun sendReplyMessage(body: MessageBodyModel) {
        if (bDataChat.firstOrNull()?.type == VouchChatType.TYPE_QUICK_REPLY) {
            removeDataChat(0)
        }
        mVouchSDK.replyMessage(body, object : ReplyMessageCallback {
            override fun onSuccess(data: MessageResponseModel) = Unit

            override fun onError(message: String) {
                eventShowMessage.value = message
            }

        })
    }

}