package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class IntentMessageModel(
    @SerializedName("confidence")
    val confidence: Double? = 0.0,
    @SerializedName("intent")
    val intent: String? = ""
)