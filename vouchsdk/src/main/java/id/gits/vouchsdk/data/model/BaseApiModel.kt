package id.gits.vouchsdk.data.model


import com.google.gson.annotations.SerializedName

data class BaseApiModel<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("code")
    val code: Int? = 0,
    @SerializedName("message")
    val message: String? = ""
)