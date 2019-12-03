package sg.vouch.vouchsdk.data.model.config.response


import com.google.gson.annotations.SerializedName


/**
 * This is a class created for handling the data on VouchDataResource
 */
data class ConfigResponseModel(
    @SerializedName("attachmentButtonColor")
    val attachmentButtonColor: String? = "",
    @SerializedName("attachmentIconColor")
    val attachmentIconColor: String? = "",
    @SerializedName("avatar")
    val avatar: String? = "",
    @SerializedName("backgroundColorChat")
    val backgroundColorChat: String? = "",
    @SerializedName("behaviour")
    val behaviour: String? = "",
    @SerializedName("borderRadius")
    val borderRadius: String? = "",
    @SerializedName("btnBgColor")
    val btnBgColor: String? = "",
    @SerializedName("bubbleChatBorderRadius")
    val bubbleChatBorderRadius: String? = "",
    @SerializedName("closeIconURL")
    val closeIconURL: String? = "",
    @SerializedName("customChannelList")
    val customChannelList: List<Any>? = emptyList(),
    @SerializedName("defaultInput")
    val defaultInput: String? = "",
    @SerializedName("delay")
    val delay: String? = "",
    @SerializedName("displayHeader")
    val displayHeader: String? = "",
    @SerializedName("domainWhiteList")
    val domainWhiteList: List<String?>? = listOf(),
    @SerializedName("fontStyle")
    val fontStyle: String? = "",
    @SerializedName("footerTextColor")
    val footerTextColor: String? = "",
    @SerializedName("forceFullScreen")
    val forceFullScreen: String? = "",
    @SerializedName("greetingButtonTitle")
    val greetingButtonTitle: String? = "",
    @SerializedName("greetingMessage")
    val greetingMessage: GreetingMessage? = GreetingMessage(),
    @SerializedName("headerBgColor")
    val headerBgColor: String? = "",
    @SerializedName("iconColor")
    val iconColor: String? = "",
    @SerializedName("inputTextBackgroundColor")
    val inputTextBackgroundColor: String? = "",
    @SerializedName("inputTextColor")
    val inputTextColor: String? = "",
    @SerializedName("leftBubbleBgColor")
    val leftBubbleBgColor: String? = "",
    @SerializedName("leftBubbleColor")
    val leftBubbleColor: String? = "",
    @SerializedName("openIconURL")
    val openIconURL: String? = "",
    @SerializedName("openImmediately")
    val openImmediately: String? = "",
    @SerializedName("poweredByVouch")
    val poweredByVouch: Boolean? = true,
    @SerializedName("rightBubbleBgColor")
    val rightBubbleBgColor: String? = "",
    @SerializedName("rightBubbleColor")
    val rightBubbleColor: String? = "",
    @SerializedName("sendButtonColor")
    val sendButtonColor: String? = "",
    @SerializedName("sendIconColor")
    val sendIconColor: String? = "",
    @SerializedName("sendIconURL")
    val sendIconURL: String? = "",
    @SerializedName("title")
    val title: String? = "",
    @SerializedName("useAttachment")
    val useAttachment: String? = "",
    @SerializedName("useNotification")
    val useNotification: String? = "",
    @SerializedName("useVoice")
    val useVoice: String? = "",
    @SerializedName("widgetButtonSize")
    val widgetButtonSize: String? = "",
    @SerializedName("widgetFailedURL")
    val widgetFailedURL: String? = "",
    @SerializedName("xButtonColor")
    val xButtonColor: String? = ""
)