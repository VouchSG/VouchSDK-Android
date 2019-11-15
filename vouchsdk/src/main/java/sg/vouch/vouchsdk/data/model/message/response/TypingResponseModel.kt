package sg.vouch.vouchsdk.data.model.message.response


import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

data class TypingResponseModel(
    @SerializedName("eventName")
    var eventName: String? = null,
    @SerializedName("message")
    var message: Message? = null
)