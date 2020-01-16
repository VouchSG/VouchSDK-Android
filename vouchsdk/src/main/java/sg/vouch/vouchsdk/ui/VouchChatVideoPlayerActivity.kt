package sg.vouch.vouchsdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener
import com.google.gson.Gson
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.utils.setImageUrl
import kotlinx.android.synthetic.main.activity_vouch_chat_video_player.*
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.utils.setImageUrlwithoutCrop

class VouchChatVideoPlayerActivity : AppCompatActivity(), OnPreparedListener, OnSeekCompletionListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vouch_chat_video_player)

        hideSystemUi()

        var type : VouchChatType = Gson().fromJson(intent.getStringExtra("type"), VouchChatType::class.java)
        if(type == VouchChatType.TYPE_IMAGE){
            imgView.setImageUrlwithoutCrop(intent.getStringExtra("url-content"))
            videoView.visibility = View.GONE
        }else{
            imgView.visibility = View.VISIBLE
            img.setImageUrl(intent.getStringExtra("url-content"))
            videoView.setVideoURI(Uri.parse(intent.getStringExtra("url-content")))
            videoView.setOnPreparedListener(this@VouchChatVideoPlayerActivity)

        }


        closeImage.setOnClickListener { onBackPressed() }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        rootView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onPause() {
        super.onPause()
        videoView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoView.release()
    }

    override fun onPrepared() {
        frameThumbnail.visibility = View.GONE
        videoView.start()
    }

    override fun onBackPressed() {
        videoView.release()
        frameThumbnail.visibility = View.VISIBLE
        progress.visibility = View.GONE
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    override fun onSeekComplete() {
        // no use
    }

    companion object {


        fun startThisActivity(activity: Activity, url: String, type : VouchChatType){
            val intent = Intent(activity, VouchChatVideoPlayerActivity::class.java).apply {
                putExtra("url-content", url)
                putExtra("type", Gson().toJson(type))
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }


    }
}
