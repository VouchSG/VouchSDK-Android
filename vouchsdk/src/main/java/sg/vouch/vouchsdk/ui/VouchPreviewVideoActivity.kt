package sg.vouch.vouchsdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.devbrackets.android.exomedia.listener.OnCompletionListener
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_vouch_chat_video_player.*
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.utils.setImageUrl
import sg.vouch.vouchsdk.utils.setImageUrlwithoutCropDetail


// create by deannrchsnl on 2020-01-31
// contact me at deannurchusnulchotimah@gmail.com


class VouchPreviewVideoActivity : AppCompatActivity(), OnPreparedListener,
    OnSeekCompletionListener, OnCompletionListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vouch_chat_video_player)

        hideSystemUi()

        videoView.setVideoURI(Uri.parse(intent.getStringExtra("url-content")))
        videoView.setOnPreparedListener(this@VouchPreviewVideoActivity)
        videoView.setOnCompletionListener(this@VouchPreviewVideoActivity)

        closePreviewImage.setOnClickListener { onBackPressed() }
        checkPreviewImage.setOnClickListener {
            var inten = Intent()
            inten.putExtra("url", intent.getStringExtra("url-content"))
            setResult(Activity.RESULT_OK,inten)
            finish()
        }

        closeImage.visibility = View.GONE
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
    }

    override fun onBackPressed() {
        videoView.release()
        frameThumbnail.visibility = View.VISIBLE
        progress.visibility = View.GONE
        super.onBackPressed()
        overridePendingTransition(0, 0)
    }

    override fun onSeekComplete() {
    }
    override fun onCompletion() {
        videoView.reset()
        videoView.setVideoURI(Uri.parse(intent.getStringExtra("url-content")))
    }
    companion object {
        fun startThisActivity(activity: Activity, url: String){
            val intent = Intent(activity, VouchPreviewVideoActivity::class.java).apply {
                putExtra("url-content", url)
            }
            activity.startActivityForResult(intent, 888)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
