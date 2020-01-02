package sg.vouch.vouchsdk.data.model.register


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on VouchDataResource
 */
data class RegisterResponseModel(
    @SerializedName("token")
    val token: String? = "",
    @SerializedName("message")
    val message: String? = "",
    @SerializedName("websocketTicket")
    val websocketTicket: String? = ""
)