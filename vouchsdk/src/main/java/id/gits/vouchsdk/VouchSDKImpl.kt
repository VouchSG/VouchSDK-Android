package id.gits.vouchsdk

import android.app.Activity
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import id.gits.vouchsdk.callback.*
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.utils.Const.CONNECTIVITY_CHANGE
import id.gits.vouchsdk.utils.Helper

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-27
 */

class VouchSDKImpl internal constructor(val application: Application, val username: String, val password: String) : VouchSDK {


    private lateinit var mVouchCore: VouchCore
    private lateinit var mVouchData: VouchData


    /**
     * This BroadCastReceiver will triggered when connection status change
     */
    private val internetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (Helper.checkConnection(context ?: return)) {
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
        mVouchCore = VouchCore.setupCore(application, username, password, callback).build()
        mVouchData = VouchData(application)
        LocalBroadcastManager.getInstance(application)
            .registerReceiver(internetReceiver, IntentFilter(CONNECTIVITY_CHANGE))
    }


    /**
     * Reconnect to socket
     * before reconnect, system will
     * re-register for get new token and new ticket
     */
    override fun reconnect(callback: VouchCallback, forceReconnect: Boolean) {
        if(forceReconnect || !isConnected()){
            mVouchCore.changeActivity(callback)
            LocalBroadcastManager.getInstance(application).registerReceiver(internetReceiver, IntentFilter(CONNECTIVITY_CHANGE))

            mVouchCore.reconnect()
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
     * @param message is content of the message
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
     * @param callback is size of list when request the data
     */
    override fun getConfig(callback: GetConfigCallback) {
        mVouchData.getConfig(callback)
    }



    /**
     * Close, disconnect from socket, and close socket
     * this function will disable broadcastreceiver too
     */
    override fun close(application: Application) {
        LocalBroadcastManager.getInstance(application).unregisterReceiver(internetReceiver)
        mVouchCore.disconnect()
        mVouchCore.close()
    }


}