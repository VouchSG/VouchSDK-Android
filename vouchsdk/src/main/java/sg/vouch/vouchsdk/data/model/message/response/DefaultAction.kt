package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on ElementListModel
 */
data class DefaultAction(
    @SerializedName("type")
    val type: String? = "",
    @SerializedName("url")
    val url: String? = ""
)