package sg.vouch.vouchsdk

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.callback.*
import sg.vouch.vouchsdk.data.model.config.response.ConfigResponseModel
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.utils.Const.CONNECTIVITY_CHANGE
import sg.vouch.vouchsdk.utils.Helper

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-27
 */

class VouchSDKImpl internal constructor(val application: Application, val username: String, val password: String, val apiKey : String) :
    VouchSDK {

    private lateinit var mVouchCore: VouchCore
    private lateinit var mVouchData: VouchData

    /**
     * This BroadCastReceiver will triggered when connection status change
     */
    private val internetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Helper.checkConnection(application)) {
                mVouchCore.reconnect()
            } else {
                disconnect()
            }
        }
    }


    /**
     * @param callback used for callback from request data from socket and api
     * This function used for init socket connection and register BroadCastReceiver,
     * The BroadCastReceiver will triggered when connection status change
     */
    override fun init(callback: VouchCallback) {
        mVouchCore = VouchCore.setupCore(application, username, password, apiKey, callback).build()
        mVouchData = VouchData(application)
        application.registerReceiver(internetReceiver, IntentFilter(CONNECTIVITY_CHANGE))
    }


    /**
     * Reconnect to socket
     * before reconnect, system will
     * re-register for get new token and new ticket
     */
    override fun reconnect(callback: VouchCallback, forceReconnect: Boolean) {
        if (forceReconnect || !isConnected()) {
            application.registerReceiver(internetReceiver, IntentFilter(CONNECTIVITY_CHANGE))
            mVouchCore.reconnect()
            mVouchCore.changeCallback(callback)
        }
    }


    /**
     * Disconnect from the socket
     */
    override fun disconnect() {
        mVouchCore.disconnect()
    }

    /**
     * Check current socket connection
     */
    override fun isConnected(): Boolean {
        return mVouchCore.isConnected()
    }


    /**
     * Send a new message
     * @param message is title of the message
     * @param callback is callback listener from the API
     */
    override fun referenceSend(message: String, callback: ReferenceSendCallback) {
        mVouchData.referenceSend(message, callback)
    }


    /**
     * Send a new message
     * @param page
     * @param pageSize is size of list when request the data
     */
    override fun getListMessages(page: Int, pageSize: Int, callback: MessageCallback) {
        mVouchData.getListMessage(page, pageSize, callback)
    }


    /**
     * Reply a new message
     * @param body
     * @param callback is size of list when request the data
     */
    override fun replyMessage(body: MessageBodyModel, callback: ReplyMessageCallback) {
        mVouchData.replyMessage(body, callback)
    }


    /**
     * Send Current user location
     * @param body
     * @param callback
     */
    override fun sendLocation(body: LocationBodyModel, callback: LocationMessageCallback) {
        mVouchData.sendLocation(body, callback)
    }

    /**
     * Send Image from gallery or camera
     * @param body
     * @param callback
     */
    override fun sendImage(body: MultipartBody.Part, callback: ImageMessageCallback) {
        mVouchData.sendImage(body, callback)
    }

    /**
     * Send audio from voice recording
     * @param body
     * @param callback
     */
    override fun sendAudio(body: SendAudioBodyModel, callback: AudioMessageCallback) {
        mVouchData.sendAudio(body, callback)
    }

    /**
     * @param callback is size of list when request the data
     */
    override fun getConfig(callback: GetConfigCallback) {
        mVouchData.getConfig(object : GetConfigCallback {
            override fun onSuccess(data: ConfigResponseModel) {
                callback.onSuccess(data)
            }

            override fun onError(message: String) {
                callback.onError(message)
            }

        })
    }

    /**
     * Register user
     * @param callback is callback listener from the API
     */
    override fun registerUser(callback: RegisterCallback) {
        mVouchData.registerUser(callback)
    }

    /**
     * Close, disconnect from socket, and close socket
     * this function will disable broadcastreceiver too
     */
    override fun close(application: Application) {
        application.unregisterReceiver(internetReceiver)
        mVouchCore.disconnect()
        mVouchCore.close()
    }
}