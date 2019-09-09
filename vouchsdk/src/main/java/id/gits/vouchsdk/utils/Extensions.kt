package id.gits.vouchsdk.utils

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.support.annotation.ColorInt
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.widget.ImageView
import com.bumptech.glide.Glide
import id.gits.vouchsdk.utils.Const.SG_LOCALE
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


fun Fragment.openDialPhone(phoneNumber: String){
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNumber")
    startActivity(intent)
}

fun Fragment.openWebUrl(url: String){
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Context.openWebUrl(url: String){
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun ImageView.setImageUrl(url: String) {
    if (url.isNotEmpty()) {
        Glide.with(this).load(url).into(this)
    }

}

fun Int.basicToColorStateList(): ColorStateList {
    val states = arrayOf(
        intArrayOf(android.R.attr.state_enabled), // enabled
        intArrayOf(-android.R.attr.state_enabled), // disabled
        intArrayOf(-android.R.attr.state_checked), // unchecked
        intArrayOf(android.R.attr.state_pressed)  // pressed
    )
    val colors = intArrayOf(this, this, this, this)
    return ColorStateList(states, colors)
}

fun String.reformatFullDate(format: String, locale: Locale = SG_LOCALE): String {
    val dateTimeMillis = if (!TextUtils.isEmpty(this)) {
        SimpleDateFormat(Const.DATE_TIME_FORMAT, locale).parse(this).time
    } else {
        System.currentTimeMillis()
    }

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateTimeMillis

    return if (dateTimeMillis != 0.toLong()) {
        SimpleDateFormat(format, locale)
            .format(calendar.time)
    } else {
        SimpleDateFormat(format, locale)
            .format(System.currentTimeMillis())
    }
}


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