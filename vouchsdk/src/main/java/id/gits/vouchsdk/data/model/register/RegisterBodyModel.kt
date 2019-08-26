package id.gits.vouchsdk.data.model.register


import com.google.gson.annotations.SerializedName

data class RegisterBodyModel(
    @SerializedName("apikey")
    val apikey: String? = null,
    @SerializedName("info")
    val info: String? = null,
    @SerializedName("password")
    val password: String? = null,
    @SerializedName("userid")
    val userid: String? = null
)