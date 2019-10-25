package sg.vouch.vouchsdk.ui

import android.media.MediaPlayer
import android.widget.ImageView
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-06
 */
interface VouchChatClickListener {

    fun onClickChatButton(type: String, data: VouchChatModel)

    fun onClickPlayAudio(mediaPlayer: MediaPlayer)

    fun onClickPlayVideo(data: VouchChatModel, imageView: ImageView)

    fun onClickQuickReply(data: MessageBodyModel)

}