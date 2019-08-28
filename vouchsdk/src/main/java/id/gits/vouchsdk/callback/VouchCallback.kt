package id.gits.vouchsdk.callback

import id.gits.vouchsdk.data.model.message.response.MessageResponseModel

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchCallback {

    fun onConnected()

    fun onReceivedNewMessage(message: MessageResponseModel)

    fun onDisconnected(isActionFromUser: Boolean)

    fun onError(message: String)

}