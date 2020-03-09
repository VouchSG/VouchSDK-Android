package sg.vouch.vouchsdk.ui

import android.net.Uri
import android.widget.ImageView
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-06
 */
interface VouchChatClickListener {

    fun onClickChatButton(type: String, data: VouchChatModel)

    fun onClickPlayAudio()

    fun onClickPlayVideo(data: VouchChatModel, imageView: ImageView, type : VouchChatType)

    fun onClickQuickReply(data: MessageBodyModel)

    fun setupMediaPlayer()

    fun onClickRetryMessage(body: MessageBodyModel, position : Int)

    fun onClickRetryMedia(msgType : String, body: MultipartBody.Part, path : String, position : Int, imageUri : Uri? = null)

    fun resetMediaPlayer()

}