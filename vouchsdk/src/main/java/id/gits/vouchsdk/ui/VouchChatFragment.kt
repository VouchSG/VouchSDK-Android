package id.gits.vouchsdk.ui


import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.gits.vouchsdk.R
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import id.gits.vouchsdk.utils.parseColor
import id.gits.vouchsdk.utils.safe
import kotlinx.android.synthetic.main.fragment_vouch_chat.*


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatFragment : Fragment() {
    private lateinit var mViewModel: VouchChatViewModel
    private lateinit var mLayoutManager: LinearLayoutManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        mViewModel = VouchChatViewModel(
            requireActivity().application,
            arguments?.getString(PARAMS_USERNAME, "").safe(),
            arguments?.getString(PARAMS_PASSWORD, "").safe()
        )
        observeLiveData()
        return inflater.inflate(R.layout.fragment_vouch_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListData()
        mViewModel.start()
    }

    private fun setupListData() {
        mLayoutManager = LinearLayoutManager(requireContext())
        recyclerViewChat.layoutManager = mLayoutManager
    }

    private fun observeLiveData() {
        mViewModel.apply {


            isRequesting.observe(this@VouchChatFragment, Observer {
                progressIndicator.visibility = if (it == true) View.VISIBLE else View.GONE
            })

            changeConnectStatus.observe(this@VouchChatFragment, Observer {
                imageIndicator.setImageResource(if (it == true) R.drawable.circle_green else R.drawable.circle_red)
            })

            eventShowMessage.observe(this@VouchChatFragment, Observer {
                Snackbar.make(view ?: return@Observer, it.safe(), Snackbar.LENGTH_LONG).show()
            })

            loadConfiguration.observe(this@VouchChatFragment, Observer {
                if (it != null) {
                    titleChat.text = it.title
                    toolbarChat.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
                    poweredText.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
                    backgroundContent.background = ColorDrawable(it.backgroundColorChat.parseColor(Color.WHITE))
                }
            })

        }
    }

    override fun onResume() {
        super.onResume()
        mViewModel.reconnectSocket()
    }

    override fun onPause() {
        super.onPause()
        mViewModel.disconnectSocket()
    }



}
