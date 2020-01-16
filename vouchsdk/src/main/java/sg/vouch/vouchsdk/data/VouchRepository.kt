package sg.vouch.vouchsdk.data

import io.reactivex.disposables.Disposable
import okhttp3.MultipartBody
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
 * This is a class created for repository VouchSDK
 */
class VouchRepository(
    private val localDataSource: VouchDataSource,
    private val remoteDataSource: VouchDataSource
) : VouchDataSource {

    override fun clearData() {
        localDataSource.clearData()
    }

    override fun saveConfig(data: ConfigResponseModel?) {
        localDataSource.saveConfig(data)
    }

    override fun sendLocation(
        token: String,
        body: LocationBodyModel,
        onSuccess: (data: Any) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.sendLocation(getApiToken(), body, onSuccess, onError, onFinish)
    }

    override fun getLocalConfig(): ConfigResponseModel? = localDataSource.getLocalConfig()

    override fun getConfig(
        apiKey: String,
        onSuccess: (data: ConfigResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.getConfig(getApiKey(), onSuccess, onError, onFinish)
    }

    override fun replyMessage(
        token: String,
        body: MessageBodyModel,
        onSuccess: (data: MessageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.replyMessage(getApiToken(), body, onSuccess, onError, onUnAuthorize, onFinish)
        saveLastMessage(body)
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
        remoteDataSource.getListMessage(getApiToken(), page, pageSize, onSuccess, onError, onFinish, onUnAuthorize)
    }

    override fun registerUser(
        token: String,
        body: RegisterBodyModel,
        onSuccess: (data: RegisterResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ): Disposable {
        return remoteDataSource.registerUser(token, body, { result ->
            result.apply {
                saveWebSocketTicket(this@apply.websocketTicket ?: "")
                saveApiToken(this@apply.token ?: "")
                saveApiKey(token)
                saveUsernameAndPassword(body.userid ?: "", body.password ?: "")
            }
            onSuccess(result)
        }, onError, onFinish)
    }

    override fun sendImage(
        token: String,
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.sendImage(getApiToken(), body, onSuccess, onError, onUnAuthorize, onFinish)
    }

    override fun sendAudio(
        token: String,
        body: SendAudioBodyModel,
        onSuccess: (data: SendAudioResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.sendAudio(getApiToken(), body, onSuccess, onError, onUnAuthorize, onFinish)
    }

    override fun referenceSend(
        token: String,
        body: ReferenceSendBodyModel,
        onSuccess: (data: String) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.referenceSend(getApiToken(), body, onSuccess, onError, onFinish)
    }

    override fun saveWebSocketTicket(token: String) {
        localDataSource.saveWebSocketTicket(token)
    }

    override fun getWebSocketTicket(): String {
        return localDataSource.getWebSocketTicket()
    }

    override fun saveApiToken(token: String) {
        localDataSource.saveApiToken(token)
    }

    override fun getApiToken(): String {
        return localDataSource.getApiToken()
    }

    override fun saveApiKey(apiKey: String) {
        localDataSource.saveApiKey(apiKey)
    }

    override fun getApiKey(): String {
        return localDataSource.getApiKey()
    }

    override fun getLastMessage(): MessageBodyModel? {
        return localDataSource.getLastMessage()
    }

    override fun saveLastMessage(body: MessageBodyModel?) {
        localDataSource.saveLastMessage(body)
    }

    override fun saveUsernameAndPassword(username: String, password: String) {
        localDataSource.saveUsernameAndPassword(username, password)
    }

    override fun getUsername(): String {
        return localDataSource.getUsername()
    }

    override fun getPassword(): String {
        return localDataSource.getPassword()
    }

    override fun revokeCredential() {
        localDataSource.revokeCredential()
    }
}