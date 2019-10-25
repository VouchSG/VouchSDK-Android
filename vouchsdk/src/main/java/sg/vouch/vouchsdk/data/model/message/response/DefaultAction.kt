package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class DefaultAction(
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("url")
    val url: String? = ""
)