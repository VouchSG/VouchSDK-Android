package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

data class GalleryElementModel(
    @SerializedName("buttons")
    val buttons: List<ButtonModel>? = listOf(),
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("subtitle")
    val subtitle: String? = null,
    @SerializedName("title")
    val title: String? = null
)