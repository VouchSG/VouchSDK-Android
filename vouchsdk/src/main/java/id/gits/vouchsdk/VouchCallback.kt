package id.gits.vouchsdk

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


interface VouchCallback {

    fun onConnected()

    fun onReceivedNewMessage(message: String)

    fun onDisconnected(isActionFromUser: Boolean)

    fun onClosed()

}