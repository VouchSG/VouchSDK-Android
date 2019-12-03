package sg.vouch.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName
import sg.vouch.vouchsdk.data.model.message.response.location.*

/**
 * This is a class created for handling the data on MessageResponseModel
 */
data class LocationBlockModel(
    @SerializedName("permissionDenied")
    val permissionDenied: PermissionDeniedModel? = null,
    @SerializedName("positionUnavailable")
    val positionUnavailable: PositionUnavailableModel? = null,
    @SerializedName("timeout")
    val timeout: TimeoutModel? = null,
    @SerializedName("unknownError")
    val unknownError: UnknownErrorModel? = null,
    @SerializedName("unsupportedBrowser")
    val unsupportedBrowser: UnsupportedBrowserModel? = null
)