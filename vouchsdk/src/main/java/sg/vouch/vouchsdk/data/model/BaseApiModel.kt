package sg.vouch.vouchsdk.data.model


import com.google.gson.annotations.SerializedName

/**
 * This is a class created for handling the response data on VouchApiService
 *
 * @param T name class of data
 */
data class BaseApiModel<T>(
    @SerializedName("data")
    val data: T?,
    @SerializedName("code")
    val code: Int? = 0,
    @SerializedName("message")
    val message: String? = ""
)