package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class BelongsToConversationResponseModel(
    @SerializedName("_id")
    val id: String? = ""
)