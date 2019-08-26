package id.gits.vouch

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import id.gits.vouchsdk.VouchCallback
import id.gits.vouchsdk.VouchCoreSDK
import id.gits.vouchsdk.utils.SendMessageCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VouchCallback {

    private lateinit var mVouchCoreSDK: VouchCoreSDK
    private var retryCount = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mVouchCoreSDK = VouchCoreSDK
            .withActivity(this, this)
            .build()
    }

    fun onClickSend(v: View) {
        mVouchCoreSDK.getDataSource().postMessage("welcome", object : SendMessageCallback {
            override fun onSuccess() {

            }

            override fun onError(message: String) {

            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        mVouchCoreSDK.disconnect(forceDisconnect = true)
        mVouchCoreSDK.close()
    }

    override fun onConnected() {
        imageIndicator.setImageResource(R.drawable.circle_green)
        retryCount = 5
    }

    override fun onReceivedNewMessage(message: String) {
        textContent.text = message
    }

    override fun onDisconnected(isActionFromUser: Boolean) {
        imageIndicator.setImageResource(R.drawable.circle_red)

        if (retryCount > 0 && !isActionFromUser) {
            Handler().postDelayed({ mVouchCoreSDK.reconnect(); retryCount-- }, 2000)
        } else if (!isActionFromUser) {
            Toast.makeText(this, "Unable connect to socket, please check your connection", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClosed() {

    }

}
