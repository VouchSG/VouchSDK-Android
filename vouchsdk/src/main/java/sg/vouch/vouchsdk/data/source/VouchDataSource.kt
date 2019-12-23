package sg.vouch.vouchsdk.data.source

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


/**
 * This is a interface created for DataSource and Repository
 */


interface VouchDataSource {

    fun clearData()

    fun getConfig(
        apiKey: String = "",
        onSuccess: (data: ConfigResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun saveConfig(data: ConfigResponseModel?)

    fun sendLocation(
        token: String,
        body: LocationBodyModel,
        onSuccess: (data: Any) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun getLocalConfig(): ConfigResponseModel?

    fun replyMessage(
        token: String = "",
        body: MessageBodyModel,
        onSuccess: (data: MessageResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onUnAuthorize: () -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun registerUser(
        token: String = "",
        body: RegisterBodyModel,
        onSuccess: (data: RegisterResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    ): Disposable

    fun referenceSend(
        token: String = "",
        body: ReferenceSendBodyModel,
        onSuccess: (data: String) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun getListMessage(
        token: String = "",
        page: Int,
        pageSize: Int,
        onSuccess: (data: List<MessageResponseModel>) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {},
        onUnAuthorize: () -> Unit = {}
    )

    fun sendImage(
        token: String = "",
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onUnAuthorize: () -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun sendAudio(
        token: String = "",
        body: SendAudioBodyModel,
        onSuccess: (data: SendAudioResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onUnAuthorize: () -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun saveWebSocketTicket(token: String)

    fun getWebSocketTicket(): String

    fun saveApiToken(token: String)

    fun getApiToken(): String

    fun saveApiKey(apiKey: String)

    fun getLastMessage(): MessageBodyModel?

    fun saveLastMessage(body: MessageBodyModel?)

    fun saveUsernameAndPassword(username: String, password: String)

    fun getUsername(): String

    fun getPassword(): String

    fun getApiKey(): String

    fun revokeCredential()

}