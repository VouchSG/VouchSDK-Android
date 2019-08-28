package id.gits.vouchsdk.data.model.message.body


import com.google.gson.annotations.SerializedName

data class ReferenceSendBodyModel(
    @SerializedName("referrence")
    val referrence: String? = ""
)