package sg.vouch.vouchsdk.callback

import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchCallback {

    fun onRegistered()

    fun onConnected()

    fun onReceivedNewMessage(message: MessageResponseModel)

    fun onTyping(typing: Boolean)

    fun onDisconnected(isActionFromUser: Boolean)

    fun onError(message: String)

}