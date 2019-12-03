package sg.vouch.vouchsdk.data.model.message.response

/**
 * This is a class created for handling the response data on VouchDataResource
 */
data class SendAudioResponseModel(

    val senderId: String,
    val belongsToConversation: String,
    val text: String,
    val msgType: String
)