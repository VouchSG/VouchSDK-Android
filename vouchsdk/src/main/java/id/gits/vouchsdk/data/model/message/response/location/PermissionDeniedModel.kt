package id.gits.vouchsdk.data.model.message.response.location


import com.google.gson.annotations.SerializedName

data class PermissionDeniedModel(
    @SerializedName("payload")
    val payload: String? = null
)