package sg.vouch.vouchsdk.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.VouchSDK
import com.theartofdev.edmodo.cropper.CropImage
import android.app.Activity
import android.net.Uri
import android.os.Environment
import net.alhazmy13.mediapicker.Video.VideoPicker
import sg.vouch.vouchsdk.utils.getImageOrientation
import sg.vouch.vouchsdk.utils.resaveBitmap
import java.io.File
import java.util.concurrent.TimeUnit


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
                val p1 = Environment.getExternalStorageDirectory().toString()
                var orientation = getImageOrientation(imageUri.path)
                //content://media/external/images/media/10092 file:///storage/emulated/0/Android/data/id.gits.vouch/cache/pickImageResult.jpeg

                println(imageUri.toString())
                if(orientation != 0){
                    var file = resaveBitmap(imageUri.path!!, p1, "pickImageResult${TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())}.jpeg", orientation.toFloat())
                    val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                            as VouchChatFragment
                    fragment.sendImageChat(Uri.fromFile(file))
                }else{
                    val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                            as VouchChatFragment
                    fragment.sendImageChat(imageUri)
                }


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
