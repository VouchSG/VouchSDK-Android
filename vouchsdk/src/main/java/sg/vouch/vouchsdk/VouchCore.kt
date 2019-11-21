package sg.vouch.vouchsdk

import android.annotation.SuppressLint
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import io.reactivex.disposables.Disposable
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_ERROR
import org.json.JSONObject
import sg.vouch.vouchsdk.BuildConfig.BASE_URL_SOCKET
import sg.vouch.vouchsdk.callback.VouchCallback
import sg.vouch.vouchsdk.data.VouchRepository
import sg.vouch.vouchsdk.data.model.message.response.MessageResponseModel
import sg.vouch.vouchsdk.data.model.message.response.TypingResponseModel
import sg.vouch.vouchsdk.data.model.register.RegisterBodyModel
import sg.vouch.vouchsdk.utils.Const.EVENT_NEW_MESSAGE
import sg.vouch.vouchsdk.utils.Const.EVENT_NEW_TYPING
import sg.vouch.vouchsdk.utils.Helper
import sg.vouch.vouchsdk.utils.Injection
import sg.vouch.vouchsdk.utils.addHeader
import java.util.*


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


class VouchCore internal constructor() {

    private var mSocket: Socket? = null
    private var mCallback: VouchCallback? = null

    private var mCredentialKey: String = ""
    private var mRepository: VouchRepository? = null
    private var isForceDisconnect = false
    private var registerDisposable: Disposable? = null

    private var isConnected = false

    private var username: String = UUID.randomUUID().toString()
    private var password: String = UUID.randomUUID().toString()
    private var apiKey: String = ""

    private val mGson = Gson()


    /*=================== Begin of Initialize and utility Function =========================*/


    /**
     * Set Credential for chat
     */
    fun setCredential(username: String, password: String, apiKey: String) {
        this@VouchCore.username = username
        this@VouchCore.password = password
        this@VouchCore.apiKey = apiKey
    }


    /**
     * initialize all fields
     */
    private fun initialize(application: Application, callback: VouchCallback) {
        mCallback = callback
        if(this@VouchCore.apiKey.isEmpty()){
            this@VouchCore.apiKey = Helper.getCredentialKey(application)
        }
        mCredentialKey = this@VouchCore.apiKey
        mRepository = Injection.createRepository(application)
    }


    fun changeCallback(callback: VouchCallback) {
        mCallback = callback
    }

    /**
     * Register user for get ticket for
     * init socket client
     */
    private fun registerUser() {
        registerDisposable?.dispose()
        registerDisposable = mRepository?.registerUser(
            body = RegisterBodyModel(mCredentialKey, "", password, username),
            token = mCredentialKey,
            onSuccess = {
                mCallback?.onRegistered()
                createSocket()

            },
            onError = {
                mCallback?.onError(it)
            }
        )
    }

    /**
     * Create Socket Client
     */
    fun createSocket() {
        isForceDisconnect = false
        mSocket?.close()

        mSocket = IO.socket("$BASE_URL_SOCKET?channel=widget&ticket=${mRepository?.getWebSocketTicket() ?: ""}")
        mSocket?.addHeader(mCredentialKey)

        mSocket?.apply {

            on(Socket.EVENT_CONNECT) {
                executeOnMainThread {
                    if (!isConnected) {
                        mCallback?.onConnected()
                    }
                }
            }

            on(EVENT_NEW_MESSAGE) {
                try {
                    val jsonObject = (it.firstOrNull() as JSONObject).toString()
                    val result = mGson.fromJson<MessageResponseModel>(jsonObject, MessageResponseModel::class.java)
                    executeOnMainThread {
                        Log.d("SocketResponse", jsonObject)
                        mCallback?.onReceivedNewMessage(result)
                    }

                } catch (e: Exception) {
                    executeOnMainThread {
                        mCallback?.onError("invalid response socket for the message object, ${e.message ?: ""}")
                    }
                }
            }

            on(EVENT_NEW_TYPING) {
                try {
                    val jsonObject = (it.firstOrNull() as JSONObject).toString()
                    val result = mGson.fromJson<TypingResponseModel>(jsonObject, TypingResponseModel::class.java)
                    executeOnMainThread {
                        Log.d("SocketResponse", jsonObject)
                        mCallback?.onTyping(result.message!!.typing)
                    }

                } catch (e: Exception) {
                    executeOnMainThread {
                        mCallback?.onError("invalid response socket for the message object, ${e.message ?: ""}")
                    }
                }
            }

            on(EVENT_ERROR) {
                isConnected = false
                executeOnMainThread {
                    if (!(it.firstOrNull()?.toString() ?: "").contains("renew_ticket")) {
//                        mCallback?.onError(it.firstOrNull()?.toString() ?: "")
                    }

                }
            }

            on(Socket.EVENT_DISCONNECT) {
                isConnected = false
                if (!isForceDisconnect && !isConnected()) {
                    reconnect()
                }

                executeOnMainThread {
                    mCallback?.onDisconnected(isForceDisconnect)
                }
            }

            this@VouchCore.connect()
        }
    }


    /*=================== End of Initialize and utility Function =========================*/


    /*========================= Begin of Connection Function =============================*/

    /**
     * connect to socket
     */
    fun connect() {
        mSocket?.connect()
    }

    /**
     * Reconnect socket with new ticket
     */
    fun reconnect() {
        registerUser()
    }

    /**
     * Disconnect from socket
     * @param forceDisconnect used for disable retry connection,
     * this param will send back to callback
     */
    fun disconnect() {
        this.isForceDisconnect = true
        mSocket?.disconnect()
    }

    /**
     * Close connection from socket
     */
    fun close() {
        mSocket?.close()
    }

    /**
     * Check current socket connection
     */
    fun isConnected(): Boolean = mSocket?.connected() ?: false


    private fun executeOnMainThread(block: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            block()
        }
    }

    /*========================== End of Connection Function ==============================*/


    companion object Builder {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: VouchCore? = null

        fun setupCore(application: Application, callback: VouchCallback): Builder {
            INSTANCE = VouchCore()
            INSTANCE?.setCredential(UUID.randomUUID().toString(), UUID.randomUUID().toString(), "")
            INSTANCE?.initialize(application, callback)
            return this
        }

        fun setupCore(application: Application, username: String, password: String, apiKey: String, callback: VouchCallback): Builder {
            INSTANCE = VouchCore()
            INSTANCE?.setCredential(username, password, apiKey)
            INSTANCE?.initialize(application, callback)
            return this
        }


        fun build(): VouchCore {
            return INSTANCE!!
        }

    }

}

