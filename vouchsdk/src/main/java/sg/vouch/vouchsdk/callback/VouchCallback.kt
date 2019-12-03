package sg.vouch.vouchsdk.callback

import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel

/**
 * This is a interface create for VouchCallback
 */

interface VouchCallback {

    fun onRegistered()

    fun onConnected()

    fun onReceivedNewMessage(message: MessageResponseModel)

    fun onTyping(typing: Boolean)

    fun onDisconnected(isActionFromUser: Boolean)

    fun onError(message: String)

}