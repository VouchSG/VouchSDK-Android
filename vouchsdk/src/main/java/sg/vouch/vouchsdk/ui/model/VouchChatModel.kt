package sg.vouch.vouchsdk.ui.model

import android.net.Uri
import okhttp3.MultipartBody
import sg.vouch.vouchsdk.data.model.message.response.GalleryElementModel
import sg.vouch.vouchsdk.data.model.message.response.QuickReplyModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-04
 */
data class VouchChatModel(
    val title: String = "",
    val subTitle: String = "",
    val isMyChat: Boolean = false,
    val type: VouchChatType = VouchChatType.TYPE_TEXT,
    var createdAt: String = "",
    val payload: String = "",
    val typeValue: String = "",
    var mediaUrl: String = "",
    val imageUri: Uri? = null,

    /* model for list and button */
    val isFirstListContent: Boolean = false,
    val isLastListContent: Boolean = false,

    /* model for list */
    val buttonTitle: String = "",
    val isHaveOutsideButton: Boolean = false,

    /* model for quick reply */
    val quickReplies: List<QuickReplyModel> = emptyList(),

    /* model for gallery */
    val galleryElements: List<GalleryElementModel> = emptyList(),
    var isPendingMessage: Boolean = false,
    val isFailedMessage: Boolean = false,
    val msgType : String = "",
    val body: MultipartBody.Part? = null,
    val path : String = "",

    /* id */
    val idSent: String = ""
)