package id.gits.vouchsdk

import android.app.Activity
import android.support.v4.app.Fragment
import id.gits.vouchsdk.callback.*
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-27
 */


/* VouchSDK, this class is first layer of vouchSDK for programmer */
interface VouchSDK {

    fun create(activity: Activity, callback: VouchCallback)

    fun create(fragment: Fragment, callback: VouchCallback)

    fun reconnect(fragment: Fragment, callback: VouchCallback, forceReconnect: Boolean = false)

    fun reconnect(activity: Activity, callback: VouchCallback, forceReconnect: Boolean = false)

    fun disconnect()

    fun close()

    fun isConnected(): Boolean

    fun referenceSend(message: String, callback: ReferenceSendCallback)

    fun getListMessages(page: Int, pageSize: Int, callback: MessageCallback)

    fun replyMessage(body: MessageBodyModel, callback: ReplyMessageCallback)

    fun getConfig(callback: GetConfigCallback)

    companion object Builder {

        private var mUsername = ""
        private var mPassword = ""

        /**
         * send user credential to SDK
         * @param username is userId for the user
         * @param password is password for the user
         */
        fun setCredential(username: String, password: String): Builder {
            mUsername = username
            mPassword = password

            return this@Builder
        }

        fun createSDK(): VouchSDK {
            return VouchSDKImpl(username = mUsername, password = mPassword)
        }

    }

}