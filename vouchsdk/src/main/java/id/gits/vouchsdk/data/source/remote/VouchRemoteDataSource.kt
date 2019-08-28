package id.gits.vouchsdk.data.source.remote

import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.source.VouchDataSource
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

object VouchRemoteDataSource : VouchDataSource {


    private val mApiService = VouchApiService.getApiService()

    override fun getConfig(token: String, onSuccess: (data: ConfigResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        val disposable = mApiService.getConfig(token = token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message?:"")
                }
            }, {
                onError(it.message?:"")
            }, {
                onFinish()
            })
    }

    override fun replyMessage(token: String, body: MessageBodyModel, onSuccess: (data: MessageResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        val disposable = mApiService.postReplyMessage(token = token, data = body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message?:"")
                }
            }, {
                onError(it.message?:"")
            }, {
                onFinish()
            })
    }

    override fun getListMessage(token: String, page: Int, pageSize: Int, onSuccess: (data: List<MessageResponseModel>) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        val disposable = mApiService.getListMessages(token = token, page = page, pageSize = pageSize)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message?:"")
                }
            }, {
                onError(it.message?:"")
            }, {
                onFinish()
            })
    }

    override fun registerUser(token: String, body: RegisterBodyModel, onSuccess: (data: RegisterResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        val disposable = mApiService.registerUser(token = token, data = body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data?.token != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message?:"")
                }
            }, {
                onError(it.message?:"")
            }, {
                onFinish()
            })
    }

    override fun referenceSend(token: String, body: ReferenceSendBodyModel, onSuccess: (data: String) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        val disposable = mApiService.referenceSend(token = token, data = body.copy(referrence = "welcome"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message?:"")
                }
            }, {
                onError(it.message?:"")
            }, {
                onFinish()
            })
    }

    override fun saveWebSocketTicket(token: String) {
        throwLocalException()
    }

    override fun getWebSocketTicket(): String {
        throwLocalException()
        return ""
    }

    override fun saveApiToken(token: String) {
        throwLocalException()
    }

    override fun getApiToken(): String {
        throwLocalException()
        return ""
    }

    override fun revokeCredential() {
        throwLocalException()
    }

    private fun throwLocalException() {
        throw Exception("This function only available on LocalDataSource")
    }

}