package id.gits.vouchsdk

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import androidx.fragment.app.Fragment
import id.gits.vouchsdk.BuildConfig.BASE_URL_SOCKET
import id.gits.vouchsdk.data.VouchRepository
import id.gits.vouchsdk.data.model.register.RegisterBodyModel
import id.gits.vouchsdk.utils.Const.EVENT_NEW_MESSAGE
import id.gits.vouchsdk.utils.Helper
import id.gits.vouchsdk.utils.Injection
import id.gits.vouchsdk.utils.addHeader
import io.socket.client.IO
import io.socket.client.Socket


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


class VouchCoreSDK internal constructor() {

    private lateinit var mSocket: Socket
    private lateinit var mCallback: VouchCallback
    private lateinit var mActivity: Activity

    private var mCredentialKey: String = ""
    private var mRepository: VouchRepository? = null
    private var isForceDisconnect = false

    private lateinit var dataSource: VouchDataSDK


    /*=================== Begin of Initialize and utility Function =========================*/


    /**
     * Get dataSource, this function used for
     * get data class
     */
    fun getDataSource(): VouchDataSDK {
        return dataSource
    }

    /**
     * initialize all fields, with fragment
     */
    private fun initialize(fragment: Fragment, callback: VouchCallback) {
        initialize(fragment.requireActivity(), callback)
    }

    /**
     * initialize all fields
     */
    private fun initialize(activity: Activity, callback: VouchCallback) {
        mCallback = callback
        mActivity = activity
        dataSource = VouchDataSDK(mActivity)

        mRepository = Injection.createRepository(mActivity)
        mCredentialKey = Helper.getCredentialKey(mActivity)

        registerUser()
    }

    /**
     * Register user for get ticket for
     * create socket client
     */
    private fun registerUser() {
        mRepository?.registerUser(
            body = RegisterBodyModel(mCredentialKey, "", "qwe123qwe123", "radhikayusuf"),
            token = mCredentialKey,
            onSuccess = {
                createSocket()
            },
            onError = {
                Log.e("Vouch-Error", it)
            }
        )
    }

    /**
     * Create Socket Client
     */
    private fun createSocket() {
        isForceDisconnect = false
        mSocket = IO.socket("$BASE_URL_SOCKET?channel=widget&ticket=${mRepository?.getWebSocketTicket() ?: ""}")
        mSocket.addHeader(mCredentialKey)

        mSocket.on(Socket.EVENT_CONNECT) {
            mActivity.runOnUiThread { mCallback.onConnected() }
        }.on(EVENT_NEW_MESSAGE) {
            mActivity.runOnUiThread { mCallback.onReceivedNewMessage(it.firstOrNull()?.toString() ?: "") }
        }.on(Socket.EVENT_DISCONNECT) {
            mActivity.runOnUiThread { mCallback.onDisconnected(isForceDisconnect) }
        }

        mSocket.connect()
    }


    /*=================== End of Initialize and utility Function =========================*/






    /*========================= Begin of Connection Function =============================*/

    /**
     * connect to socket
     */
    fun connect() {
        mSocket.connect()
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
    fun disconnect(forceDisconnect: Boolean = false) {
        this.isForceDisconnect = forceDisconnect
        mSocket.disconnect()
    }

    /**
     * Close connection from socket
     */
    fun close() {
        mSocket.disconnect()
    }

    /**
     * Check current socket connection
     */
    fun isConnected(): Boolean = mSocket.connected()

    /*========================== End of Connection Function ==============================*/



    companion object Builder {
        @SuppressLint("StaticFieldLeak")
        private var INSTANCE: VouchCoreSDK? = null

        fun withActivity(activity: Activity, callback: VouchCallback): Builder {
            if (INSTANCE == null) {
                INSTANCE = VouchCoreSDK()
                INSTANCE?.initialize(activity, callback)
            }
            return this
        }

        fun withFragment(fragment: Fragment, callback: VouchCallback): Builder {
            if (INSTANCE == null) {
                INSTANCE = VouchCoreSDK()
                INSTANCE?.initialize(fragment.requireActivity(), callback)
            }
            return this
        }

        fun build(): VouchCoreSDK {
            return INSTANCE!!
        }

    }

}

