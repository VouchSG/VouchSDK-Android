package id.gits.vouchsdk.data.source

import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchDataSource {

    fun getConfig(
        token: String = "",
        onSuccess: (data: ConfigResponseModel) -> Unit = {},
        onError: (message: String) -> Unit = {},
        onFinish: () -> Unit = {}
    )

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
    )

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

    fun saveWebSocketTicket(token: String)

    fun getWebSocketTicket(): String

    fun saveApiToken(token: String)

    fun getApiToken(): String

    fun revokeCredential()

}