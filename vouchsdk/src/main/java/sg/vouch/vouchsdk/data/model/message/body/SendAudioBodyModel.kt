package sg.vouch.vouchsdk.data.model.message.body

/**
 * This is a class created for handling the data on VouchDataResource
 */
data class SendAudioBodyModel(

    val voice: String,
    val channelCount: String = "2"
)