package id.gits.vouchsdk.utils

import android.graphics.Color
import android.support.annotation.ColorInt
import io.socket.client.Socket
import io.socket.engineio.client.Transport


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

fun Socket.addHeader(credentialToken: String) {
    on(Transport.EVENT_REQUEST_HEADERS) { args ->
        if (args.getOrNull(0) is MutableMap<*, *> && args.isNotEmpty()) {
            val headers = args[0] as MutableMap<String, String>
            headers["token"] = credentialToken
            headers["Content-Type"] = "application/json"
        }
    }
}

fun String?.parseColor(@ColorInt defaultColor: Int = Color.WHITE): Int {
    return try {
        if (this != null) {
            Color.parseColor(this)
        } else defaultColor
    } catch (e: Exception) {
        defaultColor
    }
}

fun String?.safe(default: String = ""): String {
    return this ?: default
}