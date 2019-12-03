package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on MessageResponseModel
 */
data class IntentMessageModel(
    @SerializedName("confidence")
    val confidence: Double? = 0.0,
    @SerializedName("intent")
    val intent: String? = ""
)