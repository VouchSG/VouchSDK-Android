package sg.vouch.vouch

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import sg.vouch.vouchsdk.VouchSDK
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun openChat(v: View) {
        VouchSDK
            .setCredential(username = username.text.toString(), password = password.text.toString())
            .setApiKey("")
            .isUsingEntranceAnimation(true)
            .openChatActivity(this)
    }

}
