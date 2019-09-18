package id.gits.vouchsdk.data.source.local

import android.content.Context
import com.google.gson.Gson
import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.LocationBodyModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.message.body.ReferenceSendBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.source.VouchDataSource
import id.gits.vouchsdk.utils.Const.PREF_API_KEY
import id.gits.vouchsdk.utils.Const.PREF_KEY
import id.gits.vouchsdk.utils.Const.PREF_SOCKET_TICKET
import id.gits.vouchsdk.utils.Const.PREF_USER_CONFIG
import io.reactivex.disposables.Disposable

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchLocalDataSource(mContext: Context) : VouchDataSource {


    private val mPref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    private val mGson = Gson()

    override fun clearData() {
        mPref.edit().clear().apply()
    }

    override fun sendLocation(token: String, body: LocationBodyModel, onSuccess: (data: Any) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveConfig(data: ConfigResponseModel?) {
        mPref.edit().putString(PREF_USER_CONFIG, mGson.toJson(data)).apply()
    }

    override fun getLocalConfig(): ConfigResponseModel? {
        val stringData = mPref.getString(PREF_USER_CONFIG, "{}")
        return mGson.fromJson<ConfigResponseModel?>(stringData, ConfigResponseModel::class.java)
    }

    override fun saveWebSocketTicket(token: String) {
        mPref.edit().putString(PREF_SOCKET_TICKET, token).apply()
    }

    override fun getWebSocketTicket(): String {
        return mPref.getString(PREF_SOCKET_TICKET, "") ?: ""
    }

    override fun saveApiToken(token: String) {
        mPref.edit().putString(PREF_API_KEY, token).apply()
    }

    override fun getApiToken(): String {
        return mPref.getString(PREF_API_KEY, "") ?: ""
    }

    override fun revokeCredential() {
        mPref.edit().clear().apply()
    }

    override fun getConfig(
        token: String,
        onSuccess: (data: ConfigResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    override fun getListMessage(
        token: String,
        page: Int,
        pageSize: Int,
        onSuccess: (data: List<MessageResponseModel>) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    override fun registerUser(
        token: String,
        body: RegisterBodyModel,
        onSuccess: (data: RegisterResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ): Disposable {
        throw Exception("This function only available on RemoteDataSource")
    }

    override fun referenceSend(
        token: String,
        body: ReferenceSendBodyModel,
        onSuccess: (data: String) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    override fun replyMessage(
        token: String,
        body: MessageBodyModel,
        onSuccess: (data: MessageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    private fun throwRemoteException() {
        throw Exception("This function only available on RemoteDataSource")
    }

}