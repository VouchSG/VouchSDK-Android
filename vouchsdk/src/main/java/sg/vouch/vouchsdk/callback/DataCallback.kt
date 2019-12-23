package sg.vouch.vouchsdk.callback

import sg.vouch.vouchsdk.data.model.config.response.ConfigResponseModel
import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel
import sg.vouch.vouchsdk.data.model.message.response.SendAudioResponseModel
import sg.vouch.vouchsdk.data.model.message.response.UploadImageResponseModel

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface ReferenceSendCallback {
    fun onSuccess()
    fun onError(message: String)
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface GetConfigCallback {
    fun onSuccess(data: ConfigResponseModel)
    fun onError(message: String)
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface RegisterCallback {
    fun onSuccess(token: String, socketTicket: String)
    fun onError(message: String)
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface MessageCallback {
    fun onSuccess(data: List<MessageResponseModel>)
    fun onError(message: String)
    fun onUnAuthorize()
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface ReplyMessageCallback {
    fun onSuccess(data: MessageResponseModel)
    fun onError(message: String)
    fun onUnAuthorize()
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface LocationMessageCallback {
    fun onSuccess()
    fun onError(message: String)
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface ImageMessageCallback {
    fun onSuccess(data: UploadImageResponseModel)
    fun onError(message: String)
    fun onUnAuthorize()
}

/**
 *
 * This is a interface create for VouchCallback
 *
 */
interface AudioMessageCallback {
    fun onSuccess(data: SendAudioResponseModel)
    fun onError(message: String)
    fun onUnAuthorize()
}