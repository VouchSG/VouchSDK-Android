package id.gits.vouchsdk.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import id.gits.vouchsdk.R
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.utils.Const
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import com.theartofdev.edmodo.cropper.CropImage
import android.R.attr.data
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.app.Activity

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
    }
}
