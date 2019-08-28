package id.gits.vouchsdk.ui


import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.gits.vouchsdk.R
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.callback.VouchCallback
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import id.gits.vouchsdk.utils.safe
import kotlinx.android.synthetic.main.fragment_vouch_chat.*


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatFragment : Fragment(), VouchCallback {

    lateinit var mVouchSDK: VouchSDK
    lateinit var mViewModel: VouchChatViewModel
    lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mVouchSDK = VouchSDK.setCredential(
            arguments?.getString(PARAMS_USERNAME, "").safe(),
            arguments?.getString(PARAMS_PASSWORD, "").safe()).createSDK()

        mVouchSDK.create(this@VouchChatFragment, this@VouchChatFragment)
        mViewModel = ViewModelProviders.of(this@VouchChatFragment).get(VouchChatViewModel::class.java)
        observeLiveData()

        return inflater.inflate(R.layout.fragment_vouch_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListData()
    }

    private fun setupListData() {
        mLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewChat.layoutManager = mLayoutManager
    }

    private fun observeLiveData() {
        mViewModel.isRequesting.observe(this@VouchChatFragment, Observer {

        })
    }

    override fun onResume() {
        super.onResume()
        mVouchSDK.reconnect(this, this)
    }

    override fun onPause() {
        super.onPause()
        mVouchSDK.disconnect()
    }

    override fun onConnected() {
        mViewModel.getLayoutConfiguration()
    }

    override fun onReceivedNewMessage(message: MessageResponseModel) {

    }

    override fun onDisconnected(isActionFromUser: Boolean) {

    }

    override fun onError(message: String) {

    }
}
