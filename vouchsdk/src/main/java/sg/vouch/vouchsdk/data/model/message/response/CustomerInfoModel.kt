package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class CustomerInfoModel(
    @SerializedName("firstname")
    val firstname: String? = "",
    @SerializedName("picture")
    val picture: String? = ""
)