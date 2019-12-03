package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on Model and Adapter
 */
data class QuickReplyModel(
    @SerializedName("content_type")
    val contentType: String? = "",
    @SerializedName("payload")
    val payload: String? = "",
    @SerializedName("title")
    val title: String? = ""
)