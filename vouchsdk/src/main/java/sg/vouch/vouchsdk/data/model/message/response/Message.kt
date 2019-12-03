package sg.vouch.vouchsdk.data.model.message.response

import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the data on TypingResponseModel
 */
@Keep
data class Message(
    @SerializedName("typing")
    var typing: Boolean = false
)