package sg.vouch.vouchsdk.ui

import android.media.MediaPlayer
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-06
 */
interface VouchChatClickListener {

    fun onClickChatButton(type: String, data: VouchChatModel)

    fun onClickPlayAudio(status : String)

    fun onClickPlayVideo(data: VouchChatModel, imageView: ImageView, type : VouchChatType)

    fun onClickQuickReply(data: MessageBodyModel)

    fun setupMediaPlayer(mediaPlayer: MediaPlayer, tvCount : TextView, mSeekBar: SeekBar)

}