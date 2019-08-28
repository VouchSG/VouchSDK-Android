package id.gits.vouchsdk.ui

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import id.gits.vouchsdk.utils.Injection

/**
 * @Author by Radhika Yusuf
 * Bandung, on 2019-08-28
 */
class VouchChatViewModel(application: Application) : AndroidViewModel(application) {

    private val mRepository = Injection.createRepository(application)

    val isRequesting = MutableLiveData<Boolean>().apply { value = true }


    fun getLayoutConfiguration() {
        isRequesting.value = true
        mRepository.getConfig(onSuccess = {

        })


    }
}