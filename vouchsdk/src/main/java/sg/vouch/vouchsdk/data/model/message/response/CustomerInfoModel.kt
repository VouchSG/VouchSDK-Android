package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on MessageResponseModel
 */
data class CustomerInfoModel(
    @SerializedName("firstname")
    val firstname: String? = "",
    @SerializedName("picture")
    val picture: String? = "",
    val fromMe: Boolean = false
)