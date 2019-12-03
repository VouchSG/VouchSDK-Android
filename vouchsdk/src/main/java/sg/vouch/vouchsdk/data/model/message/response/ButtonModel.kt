package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on ViewModel
 */
data class ButtonModel(
    @SerializedName("payload")
    val payload: String? = null,
    @SerializedName("paymentPrices")
    val paymentPrices: List<Any?>? = listOf(),
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("url")
    val url: String? = null
)