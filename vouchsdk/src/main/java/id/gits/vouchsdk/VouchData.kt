package id.gits.vouchsdk

import android.content.Context
import id.gits.vouchsdk.callback.*
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.utils.*

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchData internal constructor(context: Context) {

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


    fun registerAccount(credentialKey: String, username: String, password: String, callback: RegisterCallback) {
        mRepository.registerUser(
            body = RegisterBodyModel(
                apikey = credentialKey,
                info = "",
                password = password,
                userid = username
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

        })

    }

    fun replyMessage(body: MessageBodyModel, callback: ReplyMessageCallback) {
        mRepository.replyMessage(body = body, onSuccess = {
            callback.onSuccess(it)
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        })
    }


}