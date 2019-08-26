package id.gits.vouchsdk.utils


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

interface SendMessageCallback {
    fun onSuccess()
    fun onError(message: String)
}