package id.gits.vouchsdk.data.model.message.response.location


import com.google.gson.annotations.SerializedName

data class UnknownErrorModel(
    @SerializedName("payload")
    val payload: String? = null
)