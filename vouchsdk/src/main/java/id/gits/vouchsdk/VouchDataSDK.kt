package id.gits.vouchsdk

import android.content.Context
import id.gits.vouchsdk.data.model.register.MessageBodyModel
import id.gits.vouchsdk.utils.Injection
import id.gits.vouchsdk.utils.SendMessageCallback

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchDataSDK(context: Context) {

    private val mRepository = Injection.createRepository(context)

    fun postMessage(message: String, callback: SendMessageCallback) {
        mRepository.postMessage(body = MessageBodyModel(referrence = message), onSuccess = {
            callback.onSuccess()
        }, onError = {
            callback.onError(it)
        }, onFinish = {

        })
    }

}