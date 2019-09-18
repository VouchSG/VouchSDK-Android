package id.gits.vouchsdk.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import id.gits.vouchsdk.R
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.utils.Const
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME


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

}
