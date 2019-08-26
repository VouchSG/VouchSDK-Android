package id.gits.vouchsdk.data

import id.gits.vouchsdk.data.model.register.MessageBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.source.VouchDataSource

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchRepository(
    private val localDataSource: VouchDataSource,
    private val remoteDataSource: VouchDataSource
) : VouchDataSource {

    override fun registerUser(token: String, body: RegisterBodyModel, onSuccess: (data: RegisterResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.registerUser(token, body, { result ->
            result.apply {
                saveWebSocketTicket(this@apply.websocketTicket ?: "")
                saveApiToken(this@apply.token ?: "")
            }
            onSuccess(result)
        }, onError, onFinish)
    }

    override fun postMessage(token: String, body: MessageBodyModel, onSuccess: (data: String) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        remoteDataSource.postMessage(getApiToken(), body, onSuccess, onError, onFinish)
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