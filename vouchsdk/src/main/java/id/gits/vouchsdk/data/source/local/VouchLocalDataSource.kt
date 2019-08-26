package id.gits.vouchsdk.data.source.local

import android.content.Context
import id.gits.vouchsdk.data.model.register.MessageBodyModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.data.model.register.RegisterResponseModel
import id.gits.vouchsdk.data.source.VouchDataSource
import id.gits.vouchsdk.utils.Const.PREF_API_KEY
import id.gits.vouchsdk.utils.Const.PREF_CREDENTIAL_KEY
import id.gits.vouchsdk.utils.Const.PREF_KEY
import id.gits.vouchsdk.utils.Const.PREF_SOCKET_TICKET

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

class VouchLocalDataSource(private val mContext: Context) : VouchDataSource {

    private val mPref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    override fun saveWebSocketTicket(token: String) {
        mPref.edit().putString(PREF_SOCKET_TICKET, token).apply()
    }

    override fun getWebSocketTicket(): String {
        return mPref.getString(PREF_SOCKET_TICKET, "")?:""
    }

    override fun saveApiToken(token: String) {
        mPref.edit().putString(PREF_API_KEY, token).apply()
    }

    override fun getApiToken(): String {
        return mPref.getString(PREF_API_KEY, "")?:""
    }

    override fun revokeCredential() {
        mPref.edit().clear().apply()
    }


    override fun registerUser(token: String, body: RegisterBodyModel, onSuccess: (data: RegisterResponseModel) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        throwRemoteException()
    }

    override fun postMessage(token: String, body: MessageBodyModel, onSuccess: (data: String) -> Unit, onError: (message: String) -> Unit, onFinish: () -> Unit) {
        throwRemoteException()
    }

    private fun throwRemoteException() {
        throw Exception("This function only available on RemoteDataSource")
    }

}