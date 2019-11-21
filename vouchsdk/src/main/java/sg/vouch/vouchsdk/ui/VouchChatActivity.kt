package sg.vouch.vouchsdk.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.VouchSDK
import com.theartofdev.edmodo.cropper.CropImage
import android.app.Activity
import android.net.Uri
import android.util.Log
import android.widget.Toast
import net.alhazmy13.mediapicker.Video.VideoPicker
import java.io.File


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vouch_chat)
        replaceFragment()
    }

    private fun replaceFragment() {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.frameContent, VouchSDK.createChatFragment())
        }.commit()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)
            if (imageUri != null) {
                println(imageUri.toString())
                val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                        as VouchChatFragment
                fragment.sendImageChat(imageUri)
            }
        }

        else if (requestCode === VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode === Activity.RESULT_OK) {
            val mPaths = data!!.getStringArrayListExtra(VideoPicker.EXTRA_VIDEO_PATH)
            val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                    as VouchChatFragment


            fragment.sendVideoChat(Uri.fromFile(File(mPaths[0])))
        }
    }
}
