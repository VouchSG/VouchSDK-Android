package sg.vouch.vouchsdk.data.model.message.response


import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on VouchCore
 */
data class TypingResponseModel(
    @SerializedName("eventName")
    var eventName: String? = null,
    @SerializedName("message")
    var message: Message? = null
)