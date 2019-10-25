package sg.vouch.vouchsdk.utils

import android.content.Context
import sg.vouch.vouchsdk.data.VouchRepository
import sg.vouch.vouchsdk.data.source.local.VouchLocalDataSource
import sg.vouch.vouchsdk.data.source.remote.VouchRemoteDataSource


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