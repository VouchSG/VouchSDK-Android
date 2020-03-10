package sg.vouch.vouchsdk.ui

import android.Manifest
import androidx.lifecycle.Observer
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.provider.MediaStore
import com.google.android.material.snackbar.Snackbar
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.devbrackets.android.exomedia.util.StopWatch
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.fragment_vouch_chat.*
import net.alhazmy13.mediapicker.Video.VideoPicker
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.VouchSDK
import sg.vouch.vouchsdk.data.model.message.body.MessageBodyModel
import sg.vouch.vouchsdk.data.model.message.body.SendAudioBodyModel
import sg.vouch.vouchsdk.ui.VouchChatEnum.*
import sg.vouch.vouchsdk.ui.adapter.VouchChatAdapter
import sg.vouch.vouchsdk.ui.model.AttachmentDialog
import sg.vouch.vouchsdk.ui.model.VouchChatModel
import sg.vouch.vouchsdk.ui.model.VouchChatType
import sg.vouch.vouchsdk.ui.model.VouchChatType.TYPE_LIST
import sg.vouch.vouchsdk.utils.*
import sg.vouch.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_PHONE
import sg.vouch.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_POSTBACK
import sg.vouch.vouchsdk.utils.Const.CHAT_BUTTON_TYPE_WEB
import sg.vouch.vouchsdk.utils.Const.IMAGE_UPLOAD_KEY
import sg.vouch.vouchsdk.utils.Const.MIME_TYPE_JPEG
import sg.vouch.vouchsdk.utils.Const.PAGE_SIZE
import sg.vouch.vouchsdk.utils.Const.URI_SCHEME_FILE
import sg.vouch.vouchsdk.utils.Helper.timeUnitToString
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit


/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatFragment : Fragment(), TextWatcher, View.OnClickListener, VouchChatClickListener{

    private lateinit var mViewModel: VouchChatViewModel
    private lateinit var mLayoutManager: LinearLayoutManager
    private var mLocationService: FusedLocationProviderClient? = null
    private var mSendButtonStatus = false

    var isFromVideoPlayerActivity: Boolean = false


    var isSendAudio = false

    //
    private lateinit var mStopWatch: StopWatch
    private var mIsRecording = false
    private lateinit var mWaveRecorder: WaveAudioRecorder


    var startTime = 0
    //

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewModel = createViewModel()
        observeLiveData()
        return inflater.inflate(R.layout.fragment_vouch_chat, container, false)
    }

    private fun createViewModel(): VouchChatViewModel {
        return VouchChatViewModel(requireActivity().application).apply {
            mVouchSDK = VouchSDK.createSDK(requireActivity().application)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewChat.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            if (bottom < oldBottom) {
                if (isFromVideoPlayerActivity){
                    isFromVideoPlayerActivity = false
                }else{
                    Handler().post { (v as RecyclerView).scrollToPosition(0) }
                }
            }
        }
        setupListData()
        setupListener()
        mViewModel.start()



        var window = activity!!.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = resources.getColor(R.color.colorPrimaryChat)
    }

    private fun setupListener() {
        fieldContent.addTextChangedListener(this@VouchChatFragment)
        buttonGreeting.setOnClickListener(this@VouchChatFragment)
        attachmentButton.setOnClickListener(this@VouchChatFragment)
        sendButton.setOnClickListener(this@VouchChatFragment)
        videoButton.setOnClickListener(this@VouchChatFragment)
        imageButton.setOnClickListener(this@VouchChatFragment)
        recordButton.setOnClickListener(this)
        closeImage.setOnClickListener(this)
        fieldContent.setOnFocusChangeListener { view, b ->
            lyAttach.visibility = View.GONE
        }
    }

    private fun setupListData() {
        mLayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            true
        )

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
                    && !mViewModel.isLastPage
                ) {
                    mViewModel.isPaginating = true

                    Handler().postDelayed({
                        mViewModel.getChatContent()
                    }, 1000)
                }
            }
        })
        (recyclerViewChat.itemAnimator as SimpleItemAnimator).changeDuration = 0
    }

    private fun observeLiveData() {
        mViewModel.apply {

            isRequesting.observe(this@VouchChatFragment, Observer {
                progressIndicator.visibility = if (it == true) View.VISIBLE else View.GONE
            })

            eventCloseActivity.observe(this@VouchChatFragment, Observer {
                Handler().postDelayed({
//                    activity?.finish()
                }, 3000)
            })

            changeConnectStatus.observe(this@VouchChatFragment, Observer {
                imageIndicator.setImageResource(if (it == true) R.drawable.circle_green else R.drawable.circle_red)
                inputField.visibility = if (it == true && eventChangeStateToGreeting.value != true && lyAttach.visibility == View.GONE) View.VISIBLE else View.GONE
            })

            eventShowMessage.observe(this@VouchChatFragment, Observer {
                var error = it.safe()
                if(error.toLowerCase().contains("unable to resolve host")){
                    error = "The Internet connection appears to be offline"
                }else if(error.toLowerCase().contains("password is not correct")){
                    error = "Failed to load conversation"
                }else if(error.toLowerCase().contains("http 401")){
                    error = "Reconnecting to Server"
                }
                Snackbar.make(view ?: return@Observer, error, Snackbar.LENGTH_LONG).show()
            })

            loadConfiguration.observe(this@VouchChatFragment, Observer {
                if (it != null) {

                    titleChat.text = it.title
                    titleChat.setFontFamily(it.fontStyle.safe())
                    toolbarChat.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
                    poweredText.setFontFamily(it.fontStyle.safe())
                    poweredText.background = ColorDrawable(it.headerBgColor.parseColor(Color.BLACK))
//                    inputSection.visibility =
//                    poweredText.setFontFamily(it.fontStyle.safe())

                    backgroundContent.background =
                        ColorDrawable(it.backgroundColorChat.parseColor(Color.WHITE))
                    imageProfileChat.setImageUrl(it.avatar?:"")

                    inputField.setCardBackgroundColor(it.inputTextBackgroundColor.parseColor(Color.WHITE))
                    inputField.setFontFamily(it.fontStyle.safe())

                    fieldContent.setTextColor(it.inputTextColor.parseColor(Color.BLACK))
                    fieldContent.setFontFamily(it.fontStyle.safe())

                    backgroundSend.setImageDrawable(ColorDrawable(it.sendButtonColor.parseColor(Color.BLACK)))
                    backgroundAttachment.setImageDrawable(ColorDrawable(it.attachmentButtonColor.parseColor(Color.BLACK)))

                    imageSend.setColorFilter(it.sendIconColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                    imageAttachment.setColorFilter(
                        it.attachmentIconColor.parseColor(Color.WHITE),
                        PorterDuff.Mode.SRC_IN
                    )
                    videoButton.background.setColorFilter(it.attachmentButtonColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                    imageButton.background.setColorFilter(it.attachmentButtonColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                    videoButton.setColorFilter(
                        it.attachmentIconColor.parseColor(Color.WHITE),
                        PorterDuff.Mode.SRC_IN
                    )
                    imageButton.setColorFilter(
                            it.attachmentIconColor.parseColor(Color.WHITE),
                    PorterDuff.Mode.SRC_IN
                    )

                    viewBackground.background.setColorFilter(it.sendButtonColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                    recordButton.setColorFilter(it.sendIconColor.parseColor(Color.WHITE), PorterDuff.Mode.SRC_IN)

                    buttonGreeting.text = it.greetingButtonTitle ?: "Get Started"
                    buttonGreeting.background.setColorFilter(it.leftBubbleColor.parseColor(Color.BLACK), PorterDuff.Mode.SRC_IN)
                    buttonGreeting.setTextColor(it.leftBubbleColor.parseColor(Color.BLACK))
                    buttonGreeting.setFontFamily(it.fontStyle.safe())

                    textDurationRecord.setTextColor(it.headerBgColor.parseColor(Color.BLACK))

//                    frameGreeting.visibility = View.GONE
//                    if (changeConnectStatus.value == false) {
//                        inputField.visibility = View.GONE
//                    }

                    var window = activity!!.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = it.headerBgColor.parseColor(Color.BLACK)
                }
            })

            eventChangeStateToGreeting.observe(this@VouchChatFragment, Observer {
                frameGreeting.visibility = if (it == true) View.VISIBLE else View.GONE
                inputField.visibility = if (it == true) View.GONE else View.VISIBLE
            })

            eventUpdateList.observe(this@VouchChatFragment, Observer {
                if (it != null) {
                    when (it.type) {
                        TYPE_INSERTED -> {
                            mViewModel.isDataNew = true
                            if (it.startPosition == 0) recyclerViewChat.scrollToPosition(0)
                            recyclerViewChat.adapter?.notifyItemInserted(it.startPosition)
                        }
                        TYPE_UPDATE -> {
                            recyclerViewChat.adapter?.notifyItemRangeChanged(
                                it.startPosition,
                                it.endPosition ?: it.startPosition + 1
                            )
                        }
                        TYPE_REMOVE -> {
                            mViewModel.isDataNew = true
                            recyclerViewChat.adapter?.notifyItemRangeRemoved(
                                it.startPosition,
                                it.endPosition ?: it.startPosition + 1
                            )
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
                        recyclerViewChat?.scrollBy(0, mViewModel.lastScrollPosition)
                        mViewModel.lastScrollPosition = -1
                    }, 1500)
                }
            })

            eventOnSuccessAudio.observe(this@VouchChatFragment, Observer {
                lyAttach.visibility = View.GONE
                inputField.visibility = View.VISIBLE
                loadAudio.visibility = View.GONE
                recordButton.visibility = View.VISIBLE
                textDurationRecord.setText("00:00:00")
            })

            eventOnFailedAudio.observe(this@VouchChatFragment, Observer {
                loadAudio.visibility = View.GONE
                recordButton.visibility = View.VISIBLE
                textDurationRecord.setText("00:00:00")
                startRecord()
            })


        }
    }

    /**
     * The method is used to send quick reply
     * @param data MessageBodyModel
     */
    override fun onClickQuickReply(data: MessageBodyModel) {
        if (data.msgType == "location") {
            checkMapsPermissions()
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
        resetMediaPlayer()
    }

    /**
     * The method is used when audio start record
     */
    fun startRecord(){
        mWaveRecorder = WaveAudioRecorder(requireContext().externalCacheDir!!) {
            if(isSendAudio) {
                recordButton.visibility = View.GONE
                loadAudio.visibility = View.VISIBLE
                textDurationRecord.text = ""
                onGettingBase64Audio(it)
                isSendAudio = false
            }
            mStopWatch.stop()
        }
        mStopWatch = StopWatch()
        mStopWatch.setTickListener {
            val second = it / 1000
            val minute = second / 60
            val hour = minute / 60
            textDurationRecord.text =
                "${timeUnitToString(hour)}:${timeUnitToString(minute)}:${timeUnitToString(second % 60)}"
        }


        textDurationRecord.setText("00:00:00")
    }
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.buttonGreeting -> {
                buttonGreeting.text = ""
                progressBtn.visibility = View.VISIBLE
                buttonGreeting.isEnabled = false
                mViewModel.sendReference(buttonGreeting, progressBtn)
            }
            R.id.sendButton -> {
                if (mSendButtonStatus) {
                    mViewModel.sendReplyMessage(
                        MessageBodyModel(
                            msgType = "text",
                            text = fieldContent.text.trim().toString(),
                            type = "text"
                        )
                    )
                    fieldContent.setText("")
                    mSendButtonStatus = false
                } else {
                    (activity as (VouchChatActivity)).openAudio()
                }
            }
            R.id.attachmentButton -> {
//                AttachmentDialog.newInstance().show(childFragmentManager,
//                    "VouchChatFragment@AttachmentDialog")
                inputField.visibility = View.GONE
                lyAttach.visibility = View.VISIBLE
                containerAudioRec.visibility = View.GONE
                containerMediaChoose.visibility = View.VISIBLE
            }
            R.id.closeImage -> {
                lyAttach.visibility = View.GONE
                inputField.visibility = View.VISIBLE
                if (mIsRecording) {
                    recordButton.setImageResource(R.drawable.ic_mic_white_24dp)
                    mWaveRecorder.stopRecording()
                    mIsRecording = mWaveRecorder.isRecording
                    recordButton.isEnabled = true
                    mStopWatch.stop()
                }
            }R.id.recordButton -> {
            if (mIsRecording) {
                isSendAudio = true
                recordButton.isEnabled = false
                Handler().postDelayed({
                    recordButton.setImageResource(R.drawable.ic_mic_white_24dp)
                    mWaveRecorder.stopRecording()
                    mIsRecording = mWaveRecorder.isRecording
                    recordButton.isEnabled = true
                }, 500)
            } else {
                recordButton.setImageResource(R.drawable.ic_stop_24)
                mWaveRecorder.startRecording()
                mStopWatch.start()
                mIsRecording = mWaveRecorder.isRecording
            }
        }
            R.id.imageButton -> {
                (activity as (VouchChatActivity)).openMedia(true)
            }
            R.id.videoButton -> {
                (activity as (VouchChatActivity)).openMedia(false)
            }
            else -> { }
        }
    }

    /**
     * The method is used to open layout record audio
     */
    fun openAudio(){
        lyAttach.visibility = View.VISIBLE
        containerAudioRec.visibility = View.VISIBLE
        containerMediaChoose.visibility = View.GONE
        startRecord()
        inputField.visibility = View.GONE
    }

    /**
     * The method is used to take picture from gallery or camera
     */
    fun openPhoto(){
        CameraGalleryHelper.openImagePickerOption(requireActivity())
        lyAttach.visibility = View.GONE
        inputField.visibility = View.VISIBLE
    }
    /**
     * The method is used to take video from gallery or camera
     */
    fun openVideo(){
        VideoPicker.Builder(requireActivity())
            .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
            .directory(VideoPicker.Directory.DEFAULT)
            .extension(VideoPicker.Extension.MP4)
            .enableDebuggingMode(true)
            .build()
        lyAttach.visibility = View.GONE
        inputField.visibility = View.VISIBLE
    }

    /**
     * The method is used when user click button in list
     * @param type String
     * @param data VouchChatModel
     */
    override fun onClickChatButton(type: String, data: VouchChatModel) {
        when (type) {
            CHAT_BUTTON_TYPE_POSTBACK -> {
                mViewModel.sendReplyMessage(
                    MessageBodyModel(
                        msgType = "text",
                        payload = data.payload,
                        text = if (data.type == TYPE_LIST) data.buttonTitle else data.title,
                        type = "quick_reply"
                    )
                )
            }
            CHAT_BUTTON_TYPE_WEB -> {
                openWebUrl(data.payload)
            }
            CHAT_BUTTON_TYPE_PHONE -> {
                openDialPhone(data.payload)
            }
        }
    }

    private val myHandler = Handler()
    /**
     * The method is used to playing audio
     */
    override fun onClickPlayAudio() {
        mViewModel.startUpdateSong = true
        myHandler.postDelayed(updateSongTime,100)
    }

    override fun setupMediaPlayer() {
        var startTrack = false

        val position = mViewModel.bDataChat.indexOf(mViewModel.currentAudioMedia)
        val tempView = recyclerViewChat.layoutManager?.findViewByPosition(position)
        val seekBar = tempView?.findViewById<SeekBar>(R.id.seekbar)
        seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (startTrack && fromUser) {
                    mViewModel.startUpdateSong = false
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                startTrack = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                startTrack = false
                mViewModel.startUpdateSong = true
                mViewModel.audioSeek[Helper.getAudioId(mViewModel.currentAudioMedia)] = seekBar?.progress ?: 0
                val audioSeek = mViewModel.audioSeek[Helper.getAudioId(mViewModel.currentAudioMedia)] ?: 0

                if (mViewModel.mMediaPlayer?.isPlaying == true ) {
                    mViewModel.mMediaPlayer?.seekTo(audioSeek)
                    onClickPlayAudio()
                }
            }

        })
    }
    private val updateSongTime = object : Runnable {
        override fun run() {
            if (mViewModel.startUpdateSong) {
                startTime = mViewModel.mMediaPlayer?.currentPosition ?: 0
                mViewModel.audioSeek[Helper.getAudioId(mViewModel.currentAudioMedia)] = startTime
                
                var endTime = mViewModel.mMediaPlayer?.duration!! - startTime

                activity?.runOnUiThread {
                    val position = mViewModel.bDataChat.indexOf(mViewModel.currentAudioMedia)
                    val tempView = recyclerViewChat.layoutManager?.findViewByPosition(position)
                    val audioText = tempView?.findViewById<TextView>(R.id.audioText)
                    val seekBar = tempView?.findViewById<SeekBar>(R.id.seekbar)
                    audioText?.text =
                        "${Helper.timeUnitToString(TimeUnit.MILLISECONDS.toMinutes(endTime.toLong()))}:${Helper.timeUnitToString(
                            TimeUnit.MILLISECONDS.toSeconds(endTime.toLong()) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(endTime.toLong())
                            )
                        )}"
                    seekBar?.progress = startTime
                }
                myHandler.postDelayed(this, 100)
            }
        }
    }
    /**
     * The method is used to reset media player and layout audio
     */
    override fun resetMediaPlayer() {
        val position = mViewModel.bDataChat.indexOf(mViewModel.currentAudioMedia)
        val tempView = recyclerViewChat.layoutManager?.findViewByPosition(position)
        val seekBar = tempView?.findViewById<SeekBar>(R.id.seekbar)
        val playAudio = tempView?.findViewById<ImageView>(R.id.playAudio)
        mViewModel.audioSeek.map {
            mViewModel.audioSeek[it.key] = 0
        }
        mViewModel.startUpdateSong = false
        mViewModel.mMediaPlayer?.stop()
        mViewModel.mMediaPlayer?.prepareAsync()
        playAudio?.setImageDrawable(context?.getDrawable(R.drawable.ic_play_arrow_black_24dp))
        seekBar?.max = 0
        seekBar?.progress = 0
        seekBar?.setOnSeekBarChangeListener(null)
        seekBar?.isEnabled = false
        recyclerViewChat.adapter?.notifyItemChanged(mViewModel.bDataChat.indexOf(mViewModel.currentAudioMedia))
    }

    /**
     * The method is used to playing video
     * @param data VouchChatModel
     * @param imageView ImageView value ex. myPlayVideo, playVideo
     * @param type VouchChatType value ex. TYPE_TEXT, TYPE_QUICK_REPLY
     */
    override fun onClickPlayVideo(data: VouchChatModel, imageView: ImageView, type : VouchChatType) {
        isFromVideoPlayerActivity = true
        VouchChatVideoPlayerActivity.startThisActivity(requireActivity(), data.mediaUrl, type, data.imageUri)
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        s?.toString().safe().trim().isNotEmpty().let {
            if (it) {
                imageSend.setImageResource(R.drawable.ic_send_white_24dp)
                mSendButtonStatus = it
            } else {
                imageSend.setImageResource(R.drawable.ic_mic_white_24dp)
                mSendButtonStatus = !it
            }
        }

        imageSend.setColorFilter(
            mViewModel.loadConfiguration.value?.sendIconColor.parseColor(Color.BLACK),
            PorterDuff.Mode.SRC_IN
        )
    }

    /**
     * The method is used to send audio
     * @param base64Audio String value ex. /+MYxAAEaAIEeUAQAgBgNgP/////KQQ/////Lvrg+lcWYHgtjadzsbTq+yREu495tq9c6v/7vt/of7mna9v6/btUnU17Jun9/+MYxCkT26KW+YGBAj9v6vUh+zab//v/96C3/pu6H+pv//r/ycIIP4pcWWTRBBBAMXgNdbRaABQAAABRWKwgjQVX0ECmrb///+MYxBQSM0sWWYI4A++Z/////////////0rOZ3MP//7H44QEgxgdvRVMXHZseL//540B4JAvMPEgaA4/0nHjxLhRgAoAYAgA/+MYxAYIAAJfGYEQAMAJAIAQMAwX936/q/tWtv/2f/+v//6v/+7qTEFNRTMuOTkuNVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV
     */
    fun onGettingBase64Audio(base64Audio: String) {
        mViewModel.sendAudioMessage(SendAudioBodyModel(base64Audio))
    }

    /**
     * The method is used to send video
     * @param videoUri Uri value ex. file:///storage/emulated/0/DCIM/Camera/VID_20140312_171146.mp4
     */
    fun sendVideoChat(videoUri : Uri){
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val requestBody: RequestBody?
        val requestPart: MultipartBody.Part?
        val mimeType = CameraGalleryHelper.getMimeType(videoUri.path!!)
        val file = File(videoUri.path)

        requestBody = file.asRequestBody(mimeType.toMediaTypeOrNull())
        requestPart =
            MultipartBody.Part.createFormData(IMAGE_UPLOAD_KEY, file.name, requestBody)

        mViewModel.sendImageMessage("video", requestPart, videoUri.path!!)

    }

    /**
     * The method is used to send image
     * @param imageUri Uri value ex. file:///storage/emulated/0/DCIM/Camera/VID_20140312_171146.jpg
     */
    fun sendImageChat(imageUri: Uri) {
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        val resolver = context?.contentResolver
        val scheme = imageUri.scheme
        val requestBody: RequestBody?
        val requestPart: MultipartBody.Part?

        if (scheme != null && scheme == URI_SCHEME_FILE) {
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(resolver, imageUri)
                val stream = FileOutputStream(imageUri.path)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            val file = File(imageUri.path)
            requestBody = file.asRequestBody(MIME_TYPE_JPEG.toMediaTypeOrNull())
            requestPart =
                MultipartBody.Part.createFormData(IMAGE_UPLOAD_KEY, file.name, requestBody)
        } else {
            val mimeType = resolver?.getType(imageUri)
            val inputStream = resolver?.openInputStream(imageUri)
            val file = createFileFromInputStream(inputStream!!, mimeType)

            requestBody = file!!.asRequestBody(mimeType?.toMediaTypeOrNull())
            requestPart =
                MultipartBody.Part.createFormData(IMAGE_UPLOAD_KEY, file.name, requestBody)
        }

        mViewModel.sendImageMessage("image", requestPart, imageUri.path!!, imageUri)

        val bitmap = MediaStore.Images.Media.getBitmap(resolver, imageUri)
        ivPreview.setImageBitmap(bitmap)
        ivPreview.visibility = View.GONE
    }

    /**
     * The method is used to send failed message
     * @param body MessageBodyModel
     * @param position Int value ex. 0, 1
     */
    override fun onClickRetryMessage(body: MessageBodyModel, position : Int) {
        mViewModel.removeDataChat(position)
        mViewModel.sendReplyMessage(body)
    }

    /**
     * The method is used to send failed media message
     * @param msgType String
     * @param body MultipartBody.Part
     * @param path String value ex. sdcard/Images/test_image.jpg
     * @param position Int value ex. 0, 1
     * @param imageUri Uri value ex. file:///storage/emulated/0/DCIM/Camera/VID_20140312_171146.jpg
     */
    override fun onClickRetryMedia(
        msgType: String,
        body: MultipartBody.Part,
        path: String,
        position: Int,
        imageUri: Uri?
    ) {
        mViewModel.removeDataChat(position)
        mViewModel.sendImageMessage(msgType, body, path, imageUri)
    }

    private fun createFileFromInputStream(inputStream: InputStream, mimeType: String?): File? {
        try {
            val path = context?.externalCacheDir
            val fileName = "image_temp${createFileExtensionFromMimeType(mimeType)}"
            val file = File(path, fileName)

            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var length = inputStream.read(buffer)

            while (length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream.read(buffer)
            }

            outputStream.close()
            inputStream.close()

            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun createFileExtensionFromMimeType(mimeType: String?): String {
        if (mimeType == null) return ""

        if (mimeType.contains("image/")) {
            return when (val type = mimeType.removePrefix("image/")) {
                "vnd.microsoft.icon" -> ".ico"
                "svg+xml" -> ".svg"
                else -> ".$type"
            }
        }

        return ""
    }

    private fun checkMapsPermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fineLocationPermissionState = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            val coarseLocationPermissionState = ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            val mapsPermissionGranted =
                fineLocationPermissionState == PackageManager.PERMISSION_GRANTED
                        && coarseLocationPermissionState == PackageManager.PERMISSION_GRANTED
            if (!mapsPermissionGranted) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 5115
                )
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

}
