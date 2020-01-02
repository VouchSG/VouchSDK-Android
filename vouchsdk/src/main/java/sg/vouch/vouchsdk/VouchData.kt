package sg.vouch.vouchsdk

import android.content.Context
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.callback.*
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.data.model.register.RegisterBodyModel
import sg.vouch.vouchsdk.utils.Helper
import sg.vouch.vouchsdk.utils.Injection

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchData internal constructor(private val context: Context) {
    private lateinit var mVouchCore: VouchCore

    private val mRepository = Injection.createRepository(context)

    fun getLocalConfig() = mRepository.getLocalConfig()

    fun getConfig(callback: GetConfigCallback) {
        mRepository.getConfig(onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        })
    }

    fun referenceSend(message: String, callback: ReferenceSendCallback) {
        mRepository.referenceSend(body = ReferenceSendBodyModel(referrence = message), onSuccess = {
            callback.onSuccess()
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        })
    }


    fun registerUser(callback: RegisterCallback) {
        mRepository.registerUser(
            body = RegisterBodyModel(
                apikey = Helper.getCredentialKey(context),
                info = "",
                password = mRepository.getPassword(),
                userid = mRepository.getUsername()
            ), onSuccess = {
                callback.onSuccess(it.token ?: "", it.websocketTicket ?: "")
            }, onError = {
                callback.onError(it)
            }, onFinish = {

            })
    }

    fun getListMessage(page: Int, pageSize: Int, callback: MessageCallback) {

        mRepository.getListMessage(page = page, pageSize = pageSize, onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        }, onUnAuthorize = {
            callback.onUnAuthorize()
        })

    }

    fun replyMessage(body: MessageBodyModel, callback: ReplyMessageCallback) {
        mRepository.replyMessage(body = body, onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onUnAuthorize = {
            callback.onUnAuthorize()
        }, onFinish = {

        })
    }

    fun sendLocation(body: LocationBodyModel, callback: LocationMessageCallback) {
        mRepository.sendLocation(token = "", body = body, onSuccess = {
            callback.onSuccess()
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        })
    }

    fun sendImage(body: MultipartBody.Part, callback: ImageMessageCallback) {
        mRepository.sendImage(body = body, onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onUnAuthorize = {
            callback.onUnAuthorize()
        }, onFinish = {

        })
    }

    fun sendAudio(body: SendAudioBodyModel, callback: AudioMessageCallback) {
        mRepository.sendAudio(body = body, onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onUnAuthorize = {
            callback.onUnAuthorize()
        }, onFinish = {

        })
    }
}