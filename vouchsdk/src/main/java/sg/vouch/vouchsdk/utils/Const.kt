package sg.vouch.vouchsdk.utils

import java.util.*

/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */

object Const {

    const val REGISTER_TRESHOLD = 1000 * 100 * 3

    const val EVENT_NEW_MESSAGE = "newMsg"
    const val EVENT_NEW_TYPING = "typing"

    const val PREF_KEY = "vouch-pref-key"
    const val PREF_CREDENTIAL_KEY = "credential-key"
    const val PREF_TOKEN = "token"
    const val PREF_API_KEY = "api-key"
    const val PREF_LAST_MESSAGE = "last-message"
    const val PREF_SOCKET_TICKET = "socket-key-ticket"
    const val PREF_USER_CONFIG = "pref-config"
    const val CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE"
    const val PREF_USERNAME = "username-key"
    const val PREF_PASSWORD = "password-key"

    const val PARAMS_USERNAME = "username_params"
    const val PARAMS_PASSWORD = "password_params"

    const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"


    val SG_LOCALE = Locale("en", "SG")


    const val PAGE_SIZE = 20



    const val CHAT_BUTTON_TYPE_WEB = "web_url"
    const val CHAT_BUTTON_TYPE_POSTBACK = "postback"
    const val CHAT_BUTTON_TYPE_PHONE = "phone_number"
    const val CHAT_BUTTON_TYPE_ELEMENT = "element_share"

    const val MIME_TYPE_JPEG = "image/jpeg"
    const val MIME_TYPE_MP4 = "video/mp4"
    const val URI_SCHEME_FILE = "file"
    const val URI_SCHEME_CONTENT = "file"
    const val IMAGE_UPLOAD_KEY = "file"
}