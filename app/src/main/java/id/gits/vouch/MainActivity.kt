package id.gits.vouch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import id.gits.vouchsdk.VouchSDK
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun openChat(v: View) {
        VouchSDK
            .setCredential(username = username.text.toString(), password = password.text.toString())
            .isUsingEntranceAnimation(true)
            .openChatActivity(this)
    }

}
