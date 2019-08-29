package id.gits.vouch

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.callback.GetConfigCallback
import id.gits.vouchsdk.callback.VouchCallback
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.callback.ReferenceSendCallback
import id.gits.vouchsdk.callback.ReplyMessageCallback
import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun openChat(v: View){
        VouchSDK.setCredential("radhikayusuf","qwe123qwe123").openChatActivity(this)
    }

}
