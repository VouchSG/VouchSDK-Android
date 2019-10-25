package sg.vouch.vouchsdk.data.model.message.response.location


import com.google.gson.annotations.SerializedName

data class PositionUnavailableModel(
    @SerializedName("payload")
    val payload: String? = null
)