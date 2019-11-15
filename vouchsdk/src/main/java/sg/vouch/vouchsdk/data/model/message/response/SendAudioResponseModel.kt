package sg.vouch.vouchsdk.data.model.message.response

data class SendAudioResponseModel(

    val senderId: String,
    val belongsToConversation: String,
    val text: String,
    val msgType: String
)