package id.gits.vouchsdk

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import id.gits.vouchsdk.callback.*
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.ui.VouchChatActivity
import id.gits.vouchsdk.ui.VouchChatFragment
import id.gits.vouchsdk.utils.Const
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import java.util.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-27
 */


/* VouchSDK, this class is first layer of vouchSDK for programmer */
interface VouchSDK {

    fun init(callback: VouchCallback)

    fun reconnect(callback: VouchCallback, forceReconnect: Boolean = false)

    fun disconnect()

    fun close(application: Application)

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
            mUsername = if(username.isEmpty()) UUID.randomUUID().toString() else username
            mPassword = if(password.isEmpty()) UUID.randomUUID().toString() else password

            return this@Builder
        }


        fun createSDK(application: Application): VouchSDK {
            return VouchSDKImpl(application = application, username = mUsername, password = mPassword)
        }

        fun createChatFragment(): VouchChatFragment {
            return VouchChatFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAMS_USERNAME, mUsername)
                    putString(PARAMS_PASSWORD, mPassword)
                }
            }
        }

        fun openChatActivity(context: Context) {
            val intent = Intent(context, VouchChatActivity::class.java).apply {
                putExtra(PARAMS_USERNAME, mUsername)
                putExtra(PARAMS_PASSWORD, mPassword)
            }

            context.startActivity(intent)
        }

    }

}