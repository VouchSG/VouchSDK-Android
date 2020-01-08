package sg.vouch.vouchsdk.ui

import android.os.Bundle
import android.support.design.widget.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.devbrackets.android.exomedia.util.StopWatch
import kotlinx.android.synthetic.main.dialog_audio_record.*
import sg.vouch.vouchsdk.R
import kotlin.math.min

class VoiceRecordDialog : BottomSheetDialogFragment(), View.OnClickListener {

    private var mIsRecording = false

    private lateinit var mWaveRecorder: WaveAudioRecorder
    private lateinit var mCallback: VoiceRecordDialogCallback
    private lateinit var mStopWatch: StopWatch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            mCallback = parentFragment as VoiceRecordDialogCallback
        } catch (e: ClassCastException) {
            throw ClassCastException(context?.javaClass?.simpleName +
                    " must implement VoiceRecordDialogCallback")
        }

        mWaveRecorder = WaveAudioRecorder(requireContext().externalCacheDir!!) {
            mCallback.onGettingBase64Audio(it)
            mStopWatch.stop()
            dismiss()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_audio_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mStopWatch = StopWatch()
        mStopWatch.setTickListener {
            val second = it / 1000
            val minute = second / 60
            val hour = minute / 60
            textDurationRecord.text =
                "${timeUnitToString(hour)}:${timeUnitToString(minute)}:${timeUnitToString(second % 60)}"
        }

        closeImage.setOnClickListener(this)
        recordButton.setOnClickListener(this)

        textDurationRecord.setText("00:00:00")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeImage -> {
                if (mIsRecording) {
                    mWaveRecorder.stopRecording()
                    mStopWatch.stop()
                }

                dismiss()
            }
            R.id.recordButton -> {
                if (mIsRecording) {
                    mWaveRecorder.stopRecording()
                    mIsRecording = mWaveRecorder.isRecording
                    dismiss()
                } else {
                    recordButton.setImageResource(R.drawable.ic_stop_24)
                    mWaveRecorder.startRecording()
                    mStopWatch.start()
                    mIsRecording = mWaveRecorder.isRecording
                }
            }
        }
    }

    private fun timeUnitToString(timeUnit: Long): String {
        return if (timeUnit < 10) "0$timeUnit" else timeUnit.toString()
    }

    interface VoiceRecordDialogCallback {

        fun onGettingBase64Audio(base64Audio: String)
    }

    companion object {

        fun newInstance() = VoiceRecordDialog()
    }
}