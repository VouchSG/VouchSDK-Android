package id.gits.vouchsdk.data.model.message.response


import com.google.gson.annotations.SerializedName
import id.gits.vouchsdk.data.model.message.response.location.*

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