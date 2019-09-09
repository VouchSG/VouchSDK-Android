package id.gits.vouchsdk.ui


import android.arch.lifecycle.Observer
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import id.gits.vouchsdk.R
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import id.gits.vouchsdk.ui.VouchChatEnum.*
import id.gits.vouchsdk.ui.adapter.VouchChatAdapter
import id.gits.vouchsdk.ui.model.VouchChatModel
import id.gits.vouchsdk.utils.*
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_PHONE
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_POSTBACK
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_WEB
import kotlinx.android.synthetic.main.fragment_vouch_chat.*


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatFragment : Fragment(), TextWatcher, View.OnClickListener, VouchChatClickListener {

    private lateinit var mViewModel: VouchChatViewModel
    private lateinit var mLayoutManager: LinearLayoutManager


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewModel = createViewModel()
        observeLiveData()
        return inflater.inflate(R.layout.fragment_vouch_chat, container, false)
    }

    private fun createViewModel() = VouchChatViewModel(
        requireActivity().application,
        arguments?.getString(PARAMS_USERNAME, "").safe(),
        arguments?.getString(PARAMS_PASSWORD, "").safe()
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListData()
        setupTextListener()
        mViewModel.start()

        buttonGreeting.setOnClickListener(this@VouchChatFragment)
        sendButton.setOnClickListener(this@VouchChatFragment)
    }

    private fun setupTextListener() {
        fieldContent.addTextChangedListener(this@VouchChatFragment)
    }

    private fun setupListData() {
        mLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        recyclerViewChat.adapter = VouchChatAdapter(mViewModel, this@VouchChatFragment)
        recyclerViewChat.layoutManager = mLayoutManager
    }

    private fun observeLiveData() {
        mViewModel.apply {

            isRequesting.observe(this@VouchChatFragment, Observer {
                progressIndicator.visibility = if (it == true) View.VISIBLE else View.GONE
            })

            changeConnectStatus.observe(this@VouchChatFragment, Observer {
                imageIndicator.setImageResource(if (it == true) R.drawable.circle_green else R.drawable.circle_red)
                inputField.visibility = if(it == true && eventChangeStateToGreeting.value != true) View.VISIBLE else View.GONE
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
                    poweredText.visibility = if(it.poweredByVouch == true) View.VISIBLE else View.GONE
                    imageProfileChat.setImageUrl(it.avatar?:"")

                    inputField.setCardBackgroundColor(it.inputTextBackgroundColor.parseColor())
                    fieldContent.setTextColor(it.inputTextColor.parseColor())

                    backgroundSend.setImageDrawable(ColorDrawable(it.sendButtonColor.parseColor()))
                    imageSend.setColorFilter(it.sendIconColor.parseColor(), PorterDuff.Mode.SRC_IN)
                    attachmentButton.setColorFilter(it.attachmentButtonColor.parseColor(), PorterDuff.Mode.SRC_IN)

                    buttonGreeting.text = it.greetingButtonTitle?:"Get Started"
                    buttonGreeting.setBackgroundColor(it.btnBgColor.parseColor())
                    buttonGreeting.setTextColor(it.xButtonColor.parseColor())

                    frameGreeting.visibility = View.GONE
                    inputField.visibility = View.GONE
                }
            })

            eventChangeStateToGreeting.observe(this@VouchChatFragment, Observer {
                frameGreeting.visibility = if(it == true) View.VISIBLE else View.GONE
                inputField.visibility = if(it == true) View.GONE else View.VISIBLE

            })

            eventUpdateList.observe(this@VouchChatFragment, Observer {
                if (it != null) {
                    when (it.type) {
                        TYPE_INSERTED -> {
                            recyclerViewChat.adapter?.notifyItemInserted(it.startPosition)
                            recyclerViewChat.smoothScrollToPosition(0)
                        }
                        TYPE_UPDATE -> {
                            recyclerViewChat.adapter?.notifyItemRangeChanged(it.startPosition, it.endPosition?:it.startPosition + 1)
                        }
                        TYPE_REMOVE -> {
                            recyclerViewChat.adapter?.notifyItemRangeRemoved(it.startPosition, it.endPosition?:it.startPosition + 1)
                        }
                        TYPE_FORCE_UPDATE -> {
                            recyclerViewChat.adapter?.notifyDataSetChanged()
                        }
                    }
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonGreeting -> {
                mViewModel.sendReference()
            }
            R.id.sendButton -> {
                mViewModel.sendReplyMessage(MessageBodyModel(msgType = "text", text = fieldContent.text.trim().toString(), type = "text"))
                fieldContent.setText("")
            }
            else -> {
            }
        }
    }


    override fun onClickChatButton(type: String, data: VouchChatModel) {
        when (type) {
            CHAT_BUTTON_TYPE_POSTBACK -> {
                mViewModel.sendReplyMessage(MessageBodyModel(msgType = "text", payload = data.payload, text = data.content, type = "quick_reply"))
            }
            CHAT_BUTTON_TYPE_WEB -> {
                openWebUrl(data.payload)
            }
            CHAT_BUTTON_TYPE_PHONE -> {
                openDialPhone(data.payload)
            }
        }
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        s?.toString().safe().trim().isNotEmpty().let {
            sendButton.isEnabled = it
            sendButton.isClickable = it
            sendButton.isFocusable = it

            if (it) {
                imageSend.setImageResource(R.drawable.ic_send_white_24dp)
            } else {
                imageSend.setImageResource(R.drawable.ic_mic_white_24dp)
            }
        }





        imageSend.setColorFilter(mViewModel.loadConfiguration.value?.sendButtonColor.parseColor(), PorterDuff.Mode.LIGHTEN)
    }


}
