package id.gits.vouchsdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v4.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener
import id.gits.vouchsdk.R
import id.gits.vouchsdk.utils.setImageUrl
import kotlinx.android.synthetic.main.activity_vouch_chat.*
import kotlinx.android.synthetic.main.activity_vouch_chat_video_player.*

class VouchChatVideoPlayerActivity : AppCompatActivity(), OnPreparedListener, OnSeekCompletionListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vouch_chat_video_player)
        img.setImageUrl(intent.getStringExtra("url-content"))

        hideSystemUi()

        videoView.setVideoURI(Uri.parse(intent.getStringExtra("url-content")))
        videoView.setOnPreparedListener(this@VouchChatVideoPlayerActivity)
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
    }

    override fun onSeekComplete() {

    }

    companion object {


        fun startThisActivity(activity: Activity, url: String, imageView: ImageView){
            val a = ViewCompat.getTransitionName(imageView)?:""
            val option = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, imageView, a)
            val intent = Intent(activity, VouchChatVideoPlayerActivity::class.java).apply { putExtra("url-content", url) }
            activity.startActivity(intent, option.toBundle())
        }


    }
}
