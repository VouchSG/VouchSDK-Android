package sg.vouch.vouchsdk.data.source.remote

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.MultipartBody
import retrofit2.HttpException
import sg.vouch.vouchsdk.data.model.config.response.ConfigResponseModel
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel
import sg.vouch.vouchsdk.data.model.message.response.SendAudioResponseModel
import sg.vouch.vouchsdk.data.model.message.response.UploadImageResponseModel
import sg.vouch.vouchsdk.data.model.register.RegisterBodyModel
import sg.vouch.vouchsdk.data.model.register.RegisterResponseModel
import sg.vouch.vouchsdk.data.source.VouchDataSource

/**
 * This is a object created for injection
 */

object VouchRemoteDataSource : VouchDataSource {

    override fun clearData() {

    }

    override fun sendLocation(
        token: String,
        body: LocationBodyModel,
        onSuccess: (data: Any) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        val dis = mApiService.sendLocation(token = token, data = body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message ?: "")
                }
            }, {
                onError(it.message ?: "")
            }, {
                onFinish()
            })
    }

    private var mApiService = VouchApiService.getApiService()

    private var configDisposable: Disposable? = null
    private var chatDisposable: Disposable? = null

    override fun getConfig(
        apiKey: String,
        onSuccess: (data: ConfigResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        configDisposable?.dispose()
        configDisposable = mApiService.getConfig(apiKey = apiKey)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)

                    mApiService = VouchApiService.getApiServiceCustom(it.data.domainWhiteList!![0]!!)
                } else {
                    onError(it.message ?: "")
                }
            }, {
                onError(it.message ?: "")
            }, {
                onFinish()
            })
    }

    override fun replyMessage(
        token: String,
        body: MessageBodyModel,
        onSuccess: (data: MessageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        val disposable = mApiService.postReplyMessage(token = token, data = body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else if (it.code == 401) {
                    onUnAuthorize()
                } else {
                    onError(it.message ?: "")
                }
            }, {
                if (it is HttpException){
                    if (it.code() == 401) {
                        onUnAuthorize()
                    } else {
                        onError(it.message ?: "")
                    }
                }else{
                    onError(it.message ?: "")
                }
            }, {
                onFinish()
            })
    }

    override fun getListMessage(
        token: String,
        page: Int,
        pageSize: Int,
        onSuccess: (data: List<MessageResponseModel>) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit,
        onUnAuthorize: () -> Unit
    ) {
        chatDisposable?.dispose()
        chatDisposable = mApiService.getListMessages(token = token, page = page, pageSize = pageSize)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else if (it.code == 401) {
                    onUnAuthorize()
                    onError(it.message ?: "")
                } else {
                    onError(it.message ?: "")
                }
            }, {
                if (it.message!!.toLowerCase().contains("401")){
                    onUnAuthorize()
                }
                onError(it.message ?: "")
            }, {
                onFinish()
            })
    }

    override fun registerUser(
        token: String,
        body: RegisterBodyModel,
        onSuccess: (data: RegisterResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ): Disposable {
        return mApiService.registerUser(token = token, data = body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data?.token != null && it.data?.token.length > 3) {
                    onSuccess(it.data)
                } else {
                    //{"code":200,"message":"230 ms","data":{"code":401,"message":"password is not correct"}}
                    //{"code":200,"message":"241 ms","data":{"token":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXN0b21lcklkIjoiNWRmYzQ1MzRhNDk3ZDU2NGI0N2Y1Y2ZlIiwiaWF0IjoxNTc2ODI1MzA3LCJleHAiOjE1NzY4MjUzNjd9.8203dxAGw8kMIhVnyeZ-aIcxc5SARm3e88pXLsO21_Y","websocketTicket":"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJjdXN0b21lcklkIjoiNWRmYzQ1MzRhNDk3ZDU2NGI0N2Y1Y2ZlIiwiaWF0IjoxNTc2ODI1MzA3LCJleHAiOjE1NzY4MjU5MDd9.X8FSUAhJqj0HGGhSBuQ-cIKsmNuSOhooIXEfMKBBWLI"}}
                    onError(it.data?.message ?: "")
                }
            }, {
                onError(it.message ?: "")
            }, {
                onFinish()
            })
    }

    override fun referenceSend(
        token: String,
        body: ReferenceSendBodyModel,
        onSuccess: (data: String) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        val disposable = mApiService.referenceSend(token = token, data = body.copy(referrence = "welcome"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else {
                    onError(it.message ?: "")
                }
            }, {
                onError(it.message ?: "")
            }, {
                onFinish()
            })
    }

    override fun sendImage(
        token: String,
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        val disposable = mApiService.sendImage(token, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else if (it.code == 401) {
                    onUnAuthorize()
                } else {
                    onError(it.message ?: "")
                }
            }, {
                if (it is HttpException){
                    if (it.code() == 401) {
                        onUnAuthorize()
                    } else {
                        onError(it.message ?: "")
                    }
                }else{
                    onError(it.message ?: "")
                }
            }, {
                onFinish()
            })
    }

    override fun sendAudio(
        token: String,
        body: SendAudioBodyModel,
        onSuccess: (data: SendAudioResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        val disposable = mApiService.sendAudio(token, body)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.code == 200 && it.data != null) {
                    onSuccess(it.data)
                } else if (it.code == 401) {
                    onUnAuthorize()
                } else {
                    onError(it.message ?: "")
                }
            }, {
                if (it is HttpException){
                    if (it.code() == 401) {
                        onUnAuthorize()
                    } else {
                        onError(it.message ?: "")
                    }
                }else{
                    onError(it.message ?: "")
                }
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

    override fun saveApiKey(apiKey: String) {
        throwLocalException()
    }

    override fun getApiKey(): String {
        throwLocalException()
        return ""
    }

    override fun revokeCredential() {
        throwLocalException()
    }

    override fun saveConfig(data: ConfigResponseModel?) {
        throwLocalException()
    }

    override fun getLocalConfig(): ConfigResponseModel? {
        throwLocalException()
        return null
    }

    override fun getLastMessage(): MessageBodyModel? {
        throwLocalException()
        return null
    }

    override fun saveLastMessage(body: MessageBodyModel?) {
        throwLocalException()
    }

    override fun saveUsernameAndPassword(username: String, password: String) {
        throwLocalException()
    }

    override fun getUsername(): String {
        throwLocalException()
        return ""
    }

    override fun getPassword(): String {
        throwLocalException()
        return ""
    }

    private fun throwLocalException() {
        throw Exception("This function only available on LocalDataSource")
    }

}