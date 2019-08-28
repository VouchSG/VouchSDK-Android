package id.gits.vouchsdk.callback

import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface ReferenceSendCallback {
    fun onSuccess()
    fun onError(message: String)
}

interface GetConfigCallback {
    fun onSuccess(data: ConfigResponseModel)
    fun onError(message: String)
}

interface RegisterCallback {
    fun onSuccess(token: String, socketTicket: String)
    fun onError(message: String)
}

interface MessageCallback {
    fun onSuccess(data: List<MessageResponseModel>)
    fun onError(message: String)
}

interface ReplyMessageCallback {
    fun onSuccess(data: MessageResponseModel)
    fun onError(message: String)
}