package id.gits.vouchsdk.ui


import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import id.gits.vouchsdk.R
import id.gits.vouchsdk.data.model.message.body.MessageBodyModel
import id.gits.vouchsdk.ui.VouchChatEnum.*
import id.gits.vouchsdk.ui.adapter.VouchChatAdapter
import id.gits.vouchsdk.ui.model.VouchChatModel
import id.gits.vouchsdk.utils.*
import id.gits.vouchsdk.ui.model.VouchChatType.TYPE_LIST
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_PHONE
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_POSTBACK
import id.gits.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_WEB
import id.gits.vouchsdk.utils.Const.PAGE_SIZE
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.utils.Const.PARAMS_PASSWORD
import id.gits.vouchsdk.utils.Const.PARAMS_USERNAME
import kotlinx.android.synthetic.main.fragment_vouch_chat.*


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatFragment : Fragment(), TextWatcher, View.OnClickListener, VouchChatClickListener, MediaPlayer.OnPreparedListener {


    private lateinit var mViewModel: VouchChatViewModel
    private lateinit var mLayoutManager: LinearLayoutManager
    private var mMediaPlayer: MediaPlayer? = null
    private var mLocationService: FusedLocationProviderClient? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mViewModel = createViewModel()
        observeLiveData()
        return inflater.inflate(R.layout.fragment_vouch_chat, container, false)
    }


    private fun createViewModel(): VouchChatViewModel {
        return VouchChatViewModel(requireActivity().application).apply { mVouchSDK = VouchSDK.createSDK(requireActivity().application) }
    }


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

        recyclerViewChat.layoutManager = mLayoutManager
        recyclerViewChat.adapter = VouchChatAdapter(mViewModel, this@VouchChatFragment)
        recyclerViewChat.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = mLayoutManager.childCount
                val totalItemCount = mLayoutManager.itemCount
                val firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition()


                if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= PAGE_SIZE
                            && mViewModel.isRequesting.value == false)
                    && !mViewModel.isPaginating
                    && !mViewModel.isLastPage) {
                    mViewModel.isPaginating = true

                    Handler().postDelayed({
                        mViewModel.getChatContent()
                    }, 1000)

                }
            }
        })
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
                    titleChat.setFontFamily(it.fontStyle.safe())
                    toolbarChat.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
                    poweredText.setFontFamily(it.fontStyle.safe())
                    poweredText.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
                    poweredText.visibility = if(it.poweredByVouch == true) View.VISIBLE else View.GONE
                    poweredText.setFontFamily(it.fontStyle.safe())

                    backgroundContent.background = ColorDrawable(it.backgroundColorChat.parseColor(Color.WHITE))
                    imageProfileChat.setImageUrl(it.avatar?:"")

                    inputField.setCardBackgroundColor(it.inputTextBackgroundColor.parseColor())
                    inputField.setFontFamily(it.fontStyle.safe())

                    fieldContent.setTextColor(it.inputTextColor.parseColor())
                    fieldContent.setFontFamily(it.fontStyle.safe())

                    backgroundSend.setImageDrawable(ColorDrawable(it.sendButtonColor.parseColor()))
                    backgroundAttachment.setImageDrawable(ColorDrawable(it.attachmentButtonColor.parseColor()))

                    imageSend.setColorFilter(it.sendIconColor.parseColor(), PorterDuff.Mode.SRC_IN)
                    imageAttachment.setColorFilter(it.attachmentIconColor.parseColor(), PorterDuff.Mode.SRC_IN)

                    buttonGreeting.text = it.greetingButtonTitle?:"Get Started"
                    buttonGreeting.setBackgroundColor(it.btnBgColor.parseColor())
                    buttonGreeting.setTextColor(it.xButtonColor.parseColor())
                    buttonGreeting.setFontFamily(it.fontStyle.safe())

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
                            if (it.startPosition == 0) recyclerViewChat.smoothScrollToPosition(0)
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

            eventScroll.observe(this@VouchChatFragment, Observer {
                if (mViewModel.lastScrollPosition != -1) {
                    Handler().postDelayed({
                        recyclerViewChat.smoothScrollBy(0, mViewModel.lastScrollPosition)
                        mViewModel.lastScrollPosition = -1
                    }, 1500)
                }
            })

        }
    }

    override fun onClickQuickReply(data: MessageBodyModel) {
        if (data.msgType == "location") {
            checkMapsPermissions(this@VouchChatFragment)
        } else {
            mViewModel.sendReplyMessage(data)
        }
    }

    private fun createLocationListener() {
        mLocationService = LocationServices.getFusedLocationProviderClient(requireActivity())
        mLocationService?.lastLocation?.addOnSuccessListener(mViewModel)
    }

    override fun onResume() {
        super.onResume()
        mViewModel.reconnectSocket()
    }

    override fun onPause() {
        super.onPause()
        mViewModel.disconnectSocket()
        mMediaPlayer?.stop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        (recyclerViewChat.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition().let {
            mViewModel.lastScrollPosition = if(it < 0) 0 else it
        }
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
                mViewModel.sendReplyMessage(MessageBodyModel(msgType = "text", payload = data.payload, text = if(data.type == TYPE_LIST) data.buttonTitle else data.title, type = "quick_reply"))
            }
            CHAT_BUTTON_TYPE_WEB -> {
                openWebUrl(data.payload)
            }
            CHAT_BUTTON_TYPE_PHONE -> {
                openDialPhone(data.payload)
            }
        }
    }

    override fun onClickPlayAudio(mediaPlayer: MediaPlayer) {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = mediaPlayer
        mMediaPlayer?.prepareAsync()
        mMediaPlayer?.setOnPreparedListener(this@VouchChatFragment)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mMediaPlayer?.start()
    }

    override fun onClickPlayVideo(data: VouchChatModel, imageView: ImageView) {
        VouchChatVideoPlayerActivity.startThisActivity(requireActivity(), data.mediaUrl, imageView)
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

        imageSend.setColorFilter(mViewModel.loadConfiguration.value?.sendIconColor.parseColor(), PorterDuff.Mode.SRC_IN)
    }


    private fun checkMapsPermissions(fragment: Fragment): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fineLocationPermissionState = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
            val coarseLocationPermissionState = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
            val externalStoragePermissionGranted = fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
                    && coarseLocationPermissionState == PackageManager.PERMISSION_GRANTED
            if (!externalStoragePermissionGranted) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 5115)
                return false
            } else {
                if (mViewModel.currentLocation.first == 0.0 && mViewModel.currentLocation.second == 0.0) {
                    createLocationListener()
                } else {
                    mViewModel.sendLocation()
                }
            }
            return true
        }
        return true

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 5115) {
            grantResults.forEach {
                if (it != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
        }
    }
}
