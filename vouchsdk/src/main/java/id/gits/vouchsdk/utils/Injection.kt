package id.gits.vouchsdk.utils

import android.app.Application
import android.content.Context
import id.gits.vouchsdk.VouchSDK
import id.gits.vouchsdk.data.VouchRepository
import id.gits.vouchsdk.data.source.local.VouchLocalDataSource
import id.gits.vouchsdk.data.source.remote.VouchRemoteDataSource
import id.gits.vouchsdk.ui.VouchChatViewModel


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

object Injection {

    private var mRepository: VouchRepository? = null

    fun createRepository(context: Context): VouchRepository {
        mRepository?.clearData()
        mRepository = VouchRepository(VouchLocalDataSource(context), VouchRemoteDataSource)
        return mRepository!!
    }

}