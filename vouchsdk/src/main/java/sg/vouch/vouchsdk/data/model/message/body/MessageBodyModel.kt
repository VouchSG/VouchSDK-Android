package sg.vouch.vouchsdk.data.model.message.body


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on VouchDataResource
 */
data class MessageBodyModel(
    @SerializedName("msgType")
    val msgType: String? = null,
    @SerializedName("payload")
    val payload: String? = null,
    @SerializedName("text")
    val text: String? = null,
    @SerializedName("type")
    val type: String? = null
)