package sg.vouch.vouchsdk.data.model.message.response.location


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on LocationBlockModel
 */
data class UnknownErrorModel(
    @SerializedName("payload")
    val payload: String? = null
)