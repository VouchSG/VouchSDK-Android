package id.gits.vouchsdk.data.source

import id.gits.vouchsdk.data.model.register.MessageBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchDataSource {

    fun registerUser(token: String = "", body: RegisterBodyModel, onSuccess: (data: RegisterResponseModel)->Unit = {}, onError: (message: String)->Unit = {}, onFinish: ()->Unit = {})

    fun postMessage(token: String = "", body: MessageBodyModel, onSuccess: (data: String)->Unit = {}, onError: (message: String)->Unit = {}, onFinish: ()->Unit = {})

    fun saveWebSocketTicket(token: String)

    fun getWebSocketTicket(): String

    fun saveApiToken(token: String)

    fun getApiToken(): String

    fun revokeCredential()

}