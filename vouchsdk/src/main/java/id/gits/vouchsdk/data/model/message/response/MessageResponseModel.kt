package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class MessageResponseModel(
    @SerializedName("allowTranslate")
    val allowTranslate: Boolean? = false,
    @SerializedName("belongsToConversation")
    val belongsToConversation: String? = null,
    @SerializedName("buttons")
    val buttons: List<Any?>? = emptyList(),
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("disableKeyboard")
    val disableKeyboard: Boolean? = false,
    @SerializedName("intent")
    val intent: IntentMessageModel? = null,
    @SerializedName("elements")
    val elements: List<Any?>? = emptyList(),
    @SerializedName("entity")
    val entity: List<Any?>? = emptyList(),
    @SerializedName("failed")
    val failed: Boolean? = false,
    @SerializedName("locationBlock")
    val locationBlocks: LocationBlockModel? = null,
    @SerializedName("_id")
    val id: String? = null,
    @SerializedName("lists")
    val lists: List<Any?>? = emptyList(),
    @SerializedName("mediaUrl")
    val mediaUrl: String? = null,
    @SerializedName("msgType")
    val msgType: String? = null,
    @SerializedName("quick_replies")
    val quickReplies: List<QuickReplyModel>? = emptyList(),
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("translateCredential")
    val translateCredential: String? = null,
    @SerializedName("translatedLanguage")
    val translatedLanguage: String? = null,
    @SerializedName("updatedAt")
    val updatedAt: String? = null,
    @SerializedName("__v")
    val v: Int? = 0
)