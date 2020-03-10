package sg.vouch.vouchsdk.ui.model

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.dialog_chose_attachment.*
import net.alhazmy13.mediapicker.Video.VideoPicker
import sg.vouch.vouchsdk.R
import sg.vouch.vouchsdk.utils.CameraGalleryHelper


// create by deannrchsnl on 2019-11-20
// contact me at deannurchusnulchotimah@gmail.com

class AttachmentDialog  : BottomSheetDialogFragment(), View.OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_chose_attachment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        closeImage.setOnClickListener(this)
        imageButton.setOnClickListener(this)
        videoButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.closeImage -> {

                dismiss()
            }
            R.id.imageButton -> {
                CameraGalleryHelper.openImagePickerOption(requireActivity())
                dismiss()
            }
            R.id.videoButton -> {

                VideoPicker.Builder(requireActivity())
                    .mode(VideoPicker.Mode.CAMERA_AND_GALLERY)
                    .directory(VideoPicker.Directory.DEFAULT)
                    .extension(VideoPicker.Extension.MP4)
                    .enableDebuggingMode(true)
                    .build()
                dismiss()
            }
        }
    }
    companion object {

        fun newInstance() = AttachmentDialog()
    }
}