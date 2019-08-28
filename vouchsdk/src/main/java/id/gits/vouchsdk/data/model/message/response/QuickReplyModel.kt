package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class QuickReplyModel(
    @SerializedName("content_type")
    val contentType: String? = "",
    @SerializedName("payload")
    val payload: String? = "",
    @SerializedName("title")
    val title: String? = ""
)