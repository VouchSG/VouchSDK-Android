package sg.vouch.vouchsdk

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import sg.vouch.vouchsdk.callback.*
import sg.vouch.vouchsdk.data.model.message.body.LocationBodyModel
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.VouchChatActivity
import sg.vouch.vouchsdk.ui.VouchChatFragment
import sg.vouch.vouchsdk.utils.Const.PARAMS_PASSWORD
import sg.vouch.vouchsdk.utils.Const.PARAMS_USERNAME
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import java.util.*

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-27
 * this class is first layer of vouchSDK for programmer
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

    fun sendLocation(body: LocationBodyModel, callback: LocationMessageCallback)

    fun sendImage(body: MultipartBody.Part, callback: ImageMessageCallback)

    fun sendAudio(body: SendAudioBodyModel, callback: AudioMessageCallback)

    fun getConfig(callback: GetConfigCallback)

    fun registerUser(callback: RegisterCallback)

        companion object Builder {

        private var mUsername = ""
        private var mPassword = ""
        private var mApiKey = ""
        private var isUsingAnimation = false

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

        fun isUsingEntranceAnimation(isUsingAnimation: Boolean): Builder {
            this@Builder.isUsingAnimation = isUsingAnimation
            return this@Builder
        }

        fun setApiKey(apikey : String): Builder {
            mApiKey = apikey
            return this@Builder
        }

        fun createSDK(application: Application): VouchSDK {
            return VouchSDKImpl(application = application, username = mUsername, password = mPassword, apiKey = mApiKey)
        }

        fun createChatFragment(): VouchChatFragment {
            return VouchChatFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAMS_USERNAME, mUsername)
                    putString(PARAMS_PASSWORD, mPassword)
                }
            }
        }

        fun openChatActivity(activity: Activity) {
            val intent = Intent(activity, VouchChatActivity::class.java).apply {
                putExtra(PARAMS_USERNAME, mUsername)
                putExtra(PARAMS_PASSWORD, mPassword)
            }

            activity.startActivity(intent)

            if (isUsingAnimation) {
                activity.overridePendingTransition(R.anim.translate_in_anim, R.anim.translate_in_out)
            }

        }

    }

}