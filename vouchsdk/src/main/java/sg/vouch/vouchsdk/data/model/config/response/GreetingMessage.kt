package sg.vouch.vouchsdk.data.model.config.response


import com.google.gson.annotations.SerializedName

data class GreetingMessage(
    @SerializedName("text")
    val text: String? = ""
)