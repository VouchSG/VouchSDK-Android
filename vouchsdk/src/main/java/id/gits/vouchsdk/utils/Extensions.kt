package id.gits.vouchsdk.utils

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