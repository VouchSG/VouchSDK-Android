package id.gits.vouchsdk.ui.model

import id.gits.vouchsdk.data.model.message.response.QuickReplyModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-09-04
 */
data class VouchChatModel(
    val content: String,
    val isMyChat: Boolean,
    val type: VouchChatType,
    val createdAt: String,
    val payload: String = "",
    val typeValue: String = "",
    val mediaUrl: String = "",
    val quickReplies: List<QuickReplyModel> = emptyList()
)