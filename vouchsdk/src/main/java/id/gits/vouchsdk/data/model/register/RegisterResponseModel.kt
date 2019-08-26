package id.gits.vouchsdk.data.model.register


import com.google.gson.annotations.SerializedName

data class RegisterResponseModel(
    @SerializedName("token")
    val token: String? = "",
    @SerializedName("websocketTicket")
    val websocketTicket: String? = ""
)