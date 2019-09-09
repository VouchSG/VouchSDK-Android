package id.gits.vouch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import id.gits.vouchsdk.VouchSDK

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun openChat(v: View) {
        VouchSDK
//            .setCredential("radhika", "qwe123qwe123")
            .isUsingEnteranceAnimation(true)
            .openChatActivity(this)
    }

}
