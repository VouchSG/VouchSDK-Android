package sg.vouch.vouchsdk.data.model.message.body

data class SendAudioBodyModel(

    val voice: String,
    val channelCount: String = "2"
)