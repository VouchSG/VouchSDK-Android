package id.gits.vouchsdk.data.source

import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.LocationBodyModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.model.message.response.UploadImageResponseModel
import io.reactivex.disposables.Disposable
import okhttp3.MultipartBody


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchDataSource {

    fun clearData()

    fun getConfig(
        token: String = "",
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
        onFinish: () -> Unit = {})

    fun getLocalConfig(): ConfigResponseModel?

    fun replyMessage(
        token: String = "",
        body: MessageBodyModel,
        onSuccess: (data: MessageResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
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
        onFinish: () -> Unit = {}
    )

    fun sendImage(
        token: String = "",
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    )

    fun saveWebSocketTicket(token: String)

    fun getWebSocketTicket(): String

    fun saveApiToken(token: String)

    fun getApiToken(): String

    fun revokeCredential()

}