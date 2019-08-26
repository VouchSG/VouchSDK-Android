package id.gits.vouchsdk.data.model.register


import com.google.gson.annotations.SerializedName

data class MessageBodyModel(
    @SerializedName("referrence")
    val referrence: String? = ""
)