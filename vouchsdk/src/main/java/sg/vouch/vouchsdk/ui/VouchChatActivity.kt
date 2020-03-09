package sg.vouch.vouchsdk.ui

import android.Manifest
import android.Manifest.permission.*
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.VouchSDK
import com.theartofdev.edmodo.cropper.CropImage
import android.app.Activity
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StrictMode
import android.support.v4.content.ContextCompat
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

    /**
     * variable is used to indicate the type used whether from the camera or from the gallery
     * true = from camera
     * false = from gallery
     */
    var isCamera = true

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
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
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
            var uri = Uri.fromFile(File(mPaths[0]))
            VouchPreviewVideoActivity.startThisActivity(this, uri.toString())
        }
        else if(requestCode == 888){
            if(data != null) {
                val mPaths = data!!.getStringExtra("url")
                val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                        as VouchChatFragment
                fragment.sendVideoChat(Uri.parse(mPaths))
            }
        }
    }

    override fun onRequestPermissionsResult( requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        var media = 0
        for(data in permissions){
            if(data.equals(WRITE_EXTERNAL_STORAGE) || data.equals(CAMERA)){
                media = 2
            }else if(data.equals(RECORD_AUDIO)){
                media = 1
            }
        }
        if(media == 2) {
            readytoOpenMedia()
        }else if(media == 1){
            val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                    as VouchChatFragment
            fragment.openAudio()
        }
    }

    /**
     * call method open video in fragment
     */
    fun openAudio(){
        val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                as VouchChatFragment

        if(!checkPermissionAudio()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(RECORD_AUDIO), 101)
            }
        }else{
            fragment.openAudio()
        }
    }

    /**
     * The method is used to get the isCamera variable data from the fragment
     * @param isCamera Boolean value ex. true, false
     */
    fun openMedia(isCamera : Boolean){
        this.isCamera = isCamera
        if(!checkPermissionMedia()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE, CAMERA), 101)
            }
        }else{
            readytoOpenMedia()
        }
    }

    /**
     * call method open media in fragment
     */
    fun readytoOpenMedia(){
        val fragment = supportFragmentManager.findFragmentById(R.id.frameContent)
                as VouchChatFragment
        if(isCamera){
            fragment.openPhoto()
        }else{
            fragment.openVideo()
        }
    }

    fun checkPermissionMedia():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return true
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readStoragePermissionState = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            val writeStoragePermissionState = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            val externalStoragePermissionGranted = readStoragePermissionState == PackageManager.PERMISSION_GRANTED
                    && writeStoragePermissionState == PackageManager.PERMISSION_GRANTED
            if (!externalStoragePermissionGranted && ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
            return true
        }
        return true
    }

    fun checkPermissionAudio():Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val audioRecordPermissionState = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            )
            val audioRecordPermissionGranted
                    = audioRecordPermissionState == PackageManager.PERMISSION_GRANTED
            if (!audioRecordPermissionGranted) {
                return false
            }

            return true
        }

        return true
    }
}
