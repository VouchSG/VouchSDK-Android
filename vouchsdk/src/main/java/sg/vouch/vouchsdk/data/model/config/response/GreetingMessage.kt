package sg.vouch.vouchsdk.data.model.config.response


import com.google.gson.annotations.SerializedName


/**
 * This is a class created for handling the data on VouchDataResource
 */
data class GreetingMessage(
    @SerializedName("text")
    val text: String? = ""
)