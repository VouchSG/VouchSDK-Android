package sg.vouch.vouchsdk.data.model.message.body


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on VouchDataResource
 */
data class ReferenceSendBodyModel(
    @SerializedName("referrence")
    val referrence: String? = ""
)