package id.gits.vouchsdk

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import id.gits.vouchsdk.BuildConfig.BASE_URL_SOCKET
import id.gits.vouchsdk.callback.VouchCallback
import id.gits.vouchsdk.data.VouchRepository
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.utils.Const
import id.gits.vouchsdk.utils.Const.EVENT_NEW_MESSAGE
import id.gits.vouchsdk.utils.Helper
import id.gits.vouchsdk.utils.Injection
import id.gits.vouchsdk.utils.addHeader
import io.reactivex.disposables.Disposable
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.client.Socket.EVENT_ERROR
import org.json.JSONObject
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

    private val mGson = Gson()

    private var mTresholdRegister = System.currentTimeMillis()


    /*=================== Begin of Initialize and utility Function =========================*/


    /**
     * Set Credential for chat
     */
    fun setCredential(username: String, password: String) {
        this@VouchCore.username = username
        this@VouchCore.password = password
    }


    /**
     * initialize all fields
     */
    private fun initialize(application: Application, callback: VouchCallback) {
        mCallback = callback
        mRepository = Injection.createRepository(application)
        mCredentialKey = Helper.getCredentialKey(application)
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
    private fun createSocket() {
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
                    Log.wtf("Socket-Logging", jsonObject)
                    val result = mGson.fromJson<MessageResponseModel>(jsonObject, MessageResponseModel::class.java)
                    executeOnMainThread {
                        mCallback?.onReceivedNewMessage(result)
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
                        mCallback?.onError(it.firstOrNull()?.toString() ?: "")
                    }

                }
            }

            on(Socket.EVENT_DISCONNECT) {
                isConnected = false
                if (!isForceDisconnect && isConnected()) {
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


    private fun executeOnMainThread(unit: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            unit()
        }
    }

    /*========================== End of Connection Function ==============================*/


    companion object Builder {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: VouchCore? = null

        fun setupCore(application: Application, callback: VouchCallback): Builder {
            if (INSTANCE == null) {
                INSTANCE = VouchCore()
                INSTANCE?.setCredential(UUID.randomUUID().toString(), UUID.randomUUID().toString())
                INSTANCE?.initialize(application, callback)
            }
            return this
        }

        fun setupCore(application: Application, username: String, password: String, callback: VouchCallback): Builder {
            if (INSTANCE == null) {
                INSTANCE = VouchCore()
                INSTANCE?.setCredential(username, password)
                INSTANCE?.initialize(application, callback)
            }
            return this
        }


        fun build(): VouchCore {
            return INSTANCE!!
        }

    }

}

