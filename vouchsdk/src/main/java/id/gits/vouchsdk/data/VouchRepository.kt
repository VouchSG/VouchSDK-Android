package id.gits.vouchsdk.data

import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.LocationBodyModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.model.message.response.UploadImageResponseModel
import id.gits.vouchsdk.data.source.VouchDataSource
import io.reactivex.disposables.Disposable
import okhttp3.MultipartBody

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
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

    override fun sendLocation(token: String, body: LocationBodyModel, onSuccess: (data: Any) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.sendLocation(getApiToken(), body, onSuccess, onError, onFinish)
    }

    override fun getLocalConfig(): ConfigResponseModel? = localDataSource.getLocalConfig()

    override fun getConfig(token: String, onSuccess: (data: ConfigResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.getConfig(getApiToken(), onSuccess, onError, onFinish)
    }

    override fun replyMessage(token: String, body: MessageBodyModel, onSuccess: (data: MessageResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.replyMessage(getApiToken(), body, onSuccess, onError, onFinish)
    }

    override fun getListMessage(token: String, page: Int, pageSize: Int, onSuccess: (data: List<MessageResponseModel>) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.getListMessage(getApiToken(), page, pageSize, onSuccess, onError, onFinish)
    }

    override fun registerUser(token: String, body: RegisterBodyModel, onSuccess: (data: RegisterResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit): Disposable {
        return remoteDataSource.registerUser(token, body, { result ->
            result.apply {
                saveWebSocketTicket(this@apply.websocketTicket ?: "")
                saveApiToken(this@apply.token ?: "")
            }
            onSuccess(result)
        }, onError, onFinish)
    }

    override fun sendImage(
        token: String,
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        remoteDataSource.sendImage(getApiToken(), body, onSuccess, onError, onFinish)
    }

    override fun referenceSend(token: String, body: ReferenceSendBodyModel, onSuccess: (data: String) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
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

    override fun revokeCredential() {
        localDataSource.revokeCredential()
    }
}