package id.gits.vouchsdk.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.callback.GetConfigCallback
import id.gits.vouchsdk.callback.VouchCallback
import id.gits.vouchsdk.data.model.config.response.ConfigResponseModel
import id.gits.vouchsdk.data.model.message.response.MessageResponseModel

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatViewModel constructor(application: Application, username: String, password: String) : AndroidViewModel(application), VouchCallback {


    private val mVouchSDK: VouchSDK = VouchSDK.setCredential(username, password).createSDK(application)

    val isRequesting = MutableLiveData<Boolean>()
    val changeConnectStatus = MutableLiveData<Boolean>()
    val eventShowMessage = MutableLiveData<String>()
    val loadConfiguration = MutableLiveData<ConfigResponseModel>()

    fun start() {
        mVouchSDK.init(this@VouchChatViewModel)
    }

    override fun onConnected() {
        changeConnectStatus.value = true
        getLayoutConfiguration()
    }

    override fun onReceivedNewMessage(message: MessageResponseModel) {

    }

    override fun onDisconnected(isActionFromUser: Boolean) {
        changeConnectStatus.value = false
    }

    override fun onError(message: String) {
        eventShowMessage.value = message
    }

    private fun getLayoutConfiguration() {
        isRequesting.value = true
        mVouchSDK.getConfig(object : GetConfigCallback{

            override fun onSuccess(data: ConfigResponseModel) {
                loadConfiguration.value = data
                isRequesting.value = false
            }

            override fun onError(message: String) {
                isRequesting.value = false
            }
        })
    }

    fun disconnectSocket() {
        mVouchSDK.disconnect()
    }

    fun reconnectSocket(){
        mVouchSDK.reconnect(this@VouchChatViewModel, forceReconnect = true)
    }
}