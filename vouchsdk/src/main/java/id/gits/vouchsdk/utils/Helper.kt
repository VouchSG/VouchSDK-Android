package id.gits.vouchsdk.utils

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.net.NetworkInfo
import android.support.v4.content.ContextCompat.getSystemService
import android.net.ConnectivityManager




/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

object Helper {

    fun getCredentialKey(context: Context): String {
        try {
            val app =
                context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
            val bundle = app.metaData
            return bundle.getString("sg.vouch.chat", "kosong") ?: "kosong"

        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("Error", e.message ?: e.localizedMessage)
        } catch (e: NullPointerException) {
            Log.e("Error", e.message ?: e.localizedMessage)
        }
        return ""
    }

    fun checkConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val netInfo = cm.activeNetworkInfo
        //should check null because in airplane mode it will be null
        return netInfo != null && netInfo.isConnected
    }


}