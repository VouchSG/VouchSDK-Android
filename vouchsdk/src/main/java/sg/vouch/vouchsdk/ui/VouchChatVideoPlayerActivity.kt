package sg.vouch.vouchsdk.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import android.view.View
import android.widget.ImageView
import com.devbrackets.android.exomedia.listener.OnCompletionListener
import com.devbrackets.android.exomedia.listener.OnPreparedListener
import com.devbrackets.android.exomedia.listener.OnSeekCompletionListener
import com.google.gson.Gson
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.utils.setImageUrl
import kotlinx.android.synthetic.main.activity_vouch_chat_video_player.*
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.utils.setImageUrlwithoutCropDetail

class VouchChatVideoPlayerActivity : AppCompatActivity(), OnPreparedListener, OnSeekCompletionListener, OnCompletionListener {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vouch_chat_video_player)

        hideSystemUi()

        lyVideoPreview.visibility = View.GONE

        val type : VouchChatType = Gson().fromJson(intent.getStringExtra("type"), VouchChatType::class.java)
        if(type == VouchChatType.TYPE_IMAGE){
            if(intent.getParcelableExtra<Uri>("uri-image") != null){
                imgView.setImageUrlwithoutCropDetail(intent.getParcelableExtra("uri-image")?:"")
            } else{
                imgView.setImageUrlwithoutCropDetail(intent.getStringExtra("url-content")?:"")
            }
            videoView.visibility = View.GONE
        }else{
            imgView.visibility = View.VISIBLE
            img.setImageUrl(intent.getStringExtra("url-content"))
            videoView.setVideoURI(Uri.parse(intent.getStringExtra("url-content")))
            videoView.setOnPreparedListener(this@VouchChatVideoPlayerActivity)
            videoView.setOnCompletionListener(this@VouchChatVideoPlayerActivity)
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


        fun startThisActivity(activity: Activity, url: String, type : VouchChatType, imageUri: Uri? = null){
            val intent = Intent(activity, VouchChatVideoPlayerActivity::class.java).apply {
                putExtra("url-content", url)
                putExtra("type", Gson().toJson(type))
                putExtra("uri-image", imageUri)
            }
            activity.startActivity(intent)
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
