package sg.vouch.vouchsdk.data.model.message.response

import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Message(
    @SerializedName("typing")
    var typing: Boolean = false
)