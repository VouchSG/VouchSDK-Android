package sg.vouch.vouchsdk.data.source.local

import android.content.Context
import com.google.gson.Gson
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
import sg.vouch.vouchsdk.utils.Const.PREF_API_KEY
import sg.vouch.vouchsdk.utils.Const.PREF_KEY
import sg.vouch.vouchsdk.utils.Const.PREF_LAST_MESSAGE
import sg.vouch.vouchsdk.utils.Const.PREF_PASSWORD
import sg.vouch.vouchsdk.utils.Const.PREF_SOCKET_TICKET
import sg.vouch.vouchsdk.utils.Const.PREF_TOKEN
import sg.vouch.vouchsdk.utils.Const.PREF_USERNAME
import sg.vouch.vouchsdk.utils.Const.PREF_USER_CONFIG
import sg.vouch.vouchsdk.utils.Helper

/**
 * This is a class created for handling the data on Injection
 *
 * @param context to get preference
 */

class VouchLocalDataSource(private val mContext: Context) : VouchDataSource {

    private val mPref = mContext.getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)
    private val mGson = Gson()

    override fun clearData() {
        mPref.edit().clear().apply()
    }

    override fun sendLocation(
        token: String,
        body: LocationBodyModel,
        onSuccess: (data: Any) -> Unit,
        onError: (message: String) -> Unit,
        onFinish: () -> Unit
    ) {

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
        mPref.edit().putString(PREF_TOKEN, token).apply()
    }

    override fun getApiToken(): String {
        return mPref.getString(PREF_TOKEN, "") ?: ""
    }

    override fun saveApiKey(apiKey: String) {
        mPref.edit().putString(PREF_API_KEY, apiKey).apply()
    }

    override fun getApiKey(): String {
        return mPref.getString(PREF_API_KEY, Helper.getCredentialKey(mContext)) ?: ""
    }

    override fun getLastMessage(): MessageBodyModel? {
        val stringData = mPref.getString(PREF_LAST_MESSAGE, "{}")
        return mGson.fromJson<MessageBodyModel>(stringData, MessageBodyModel::class.java)
    }

    override fun saveLastMessage(body: MessageBodyModel?) {
        mPref.edit().putString(PREF_LAST_MESSAGE, mGson.toJson(body)).apply()
    }

    override fun saveUsernameAndPassword(username: String, password: String) {
        mPref.edit().putString(PREF_USERNAME, username).apply()
        mPref.edit().putString(PREF_PASSWORD, password).apply()
    }

    override fun getUsername(): String {
        return mPref.getString(PREF_USERNAME, "") ?: ""
    }

    override fun getPassword(): String {
        return mPref.getString(PREF_PASSWORD, "") ?: ""
    }

    override fun revokeCredential() {
        mPref.edit().clear().apply()
    }

    override fun getConfig(
        apiKey: String,
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
        onFinish: () -> Unit,
        onUnAuthorize: () -> Unit
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
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    override fun sendImage(
        token: String,
        body: MultipartBody.Part,
        onSuccess: (data: UploadImageResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    override fun sendAudio(
        token: String,
        body: SendAudioBodyModel,
        onSuccess: (data: SendAudioResponseModel) -> Unit,
        onError: (message: String) -> Unit,
        onUnAuthorize: () -> Unit,
        onFinish: () -> Unit
    ) {
        throwRemoteException()
    }

    private fun throwRemoteException() {
        throw Exception("This function only available on RemoteDataSource")
    }
}