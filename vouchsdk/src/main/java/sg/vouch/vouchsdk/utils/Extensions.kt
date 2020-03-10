package sg.vouch.vouchsdk.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import sg.vouch.vouchsdk.utils.Const.SG_LOCALE
import io.socket.client.Socket
import io.socket.engineio.client.Transport
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*


/**
 * @author Radhika Yusuf Alifiansyah
 * Bandung, 26 Aug 2019
 */


fun Fragment.openDialPhone(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL)
    intent.data = Uri.parse("tel:$phoneNumber")
    startActivity(intent)
}

fun Fragment.openWebUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

fun Context.openWebUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    startActivity(intent)
}

    fun ImageView.setImageUrl(url: String) {
        Glide.with(this).load(url).centerCrop().into(this)
    }

fun ImageView.setVideoUrlwithoutCrop(url: String, icon : ImageButton, progress : ProgressBar) {
    var image = this
    icon.visibility = View.GONE
    progress.visibility = View.VISIBLE
    val bitmap = Bitmap.createBitmap(
        500, // Width
        500, // Height
        Bitmap.Config.ARGB_8888 // Config
    )
    image.setImageBitmap(bitmap)
//    Glide.with(this).load(url).into(this)
    Glide.with(this).asBitmap().load(url).placeholder(createCircularProgressDrawable(this.context)).apply(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)).override(
        400).into(object : CustomViewTarget<ImageView, Bitmap>(this) {
        override fun onLoadFailed(errorDrawable: Drawable?) {
//            icon.visibility = View.VISIBLE
//            progress.visibility = View.GONE
        }

        override fun onResourceCleared(placeholder: Drawable?) {

        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            image.setImageBitmap(resource)
            icon.visibility = View.VISIBLE
            progress.visibility = View.GONE
        }
    })
}

fun ImageView.setImageUrlwithoutCrop(url: Any) {
    val bitmap = Bitmap.createBitmap(
                500, // Width
                500, // Height
                Bitmap.Config.ARGB_8888 // Config
            )

            this.setImageBitmap(bitmap)
    Glide.with(this).load(url).into(this)
//    Glide.with(this).asBitmap().load(url).apply(RequestOptions().format(DecodeFormat.PREFER_ARGB_8888)).override(
//        400).into(object : CustomViewTarget<ImageView, Bitmap>(this) {
//        override fun onLoadFailed(errorDrawable: Drawable?) {
//            val bitmap = Bitmap.createBitmap(
//                500, // Width
//                500, // Height
//                Bitmap.Config.ARGB_8888 // Config
//            )
//
//            image.setImageBitmap(bitmap)
//        }
//
//        override fun onResourceCleared(placeholder: Drawable?) {
//
//        }
//
//        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//            image.setImageBitmap(resource)
//        }
//    })
}

fun ImageView.setImageUrlwithoutCropDetail(url: Any) {
    val bitmap = Bitmap.createBitmap(
        500, // Width
        500, // Height
        Bitmap.Config.ARGB_8888 // Config
    )

    this.setImageBitmap(bitmap)
    Glide.with(this).load(url).into(this)
//    Glide.with(this).asBitmap().load(url).into(object : CustomViewTarget<ImageView, Bitmap>(this) {
//        override fun onLoadFailed(errorDrawable: Drawable?) {
//            val bitmap = Bitmap.createBitmap(
//                500, // Width
//                500, // Height
//                Bitmap.Config.ARGB_8888 // Config
//            )
//
//            image.setImageBitmap(bitmap)
//        }
//
//        override fun onResourceCleared(placeholder: Drawable?) {
//
//        }
//
//        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
//            image.setImageBitmap(resource)
//        }
//    })
}

fun Activity.getScreenWidth(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.widthPixels
}

fun View.setFontFamily(font: String) {
    if (font.isEmpty()) {
        return
    }

    try {
        when (this) {
            is TextView -> {
                typeface = Typeface.createFromAsset(context.resources.assets, "fonts/${font.toLowerCase()}.ttf")
            }
            is EditText -> {
                typeface = Typeface.createFromAsset(context.resources.assets, "fonts/${font.toLowerCase()}.ttf")
            }
            is RadioButton -> {
                typeface = Typeface.createFromAsset(context.resources.assets, "fonts/${font.toLowerCase()}.ttf")
            }
            is Button -> {
                typeface = Typeface.createFromAsset(context.resources.assets, "fonts/${font.toLowerCase()}.ttf")
            }
        }
    } catch (e: Exception) {
        showErrorInflateFont()
    }
}

private fun showErrorInflateFont() = Log.e("FONTFACE", "error when set font face")

fun Activity.getScreenHeight(): Int {
    val displayMetrics = DisplayMetrics()
    windowManager.defaultDisplay.getMetrics(displayMetrics)
    return displayMetrics.heightPixels
}

fun createCircularProgressDrawable(context: Context): CircularProgressDrawable {
    val circularProgressDrawable =
        CircularProgressDrawable(context)
    circularProgressDrawable.strokeWidth = 4f
    circularProgressDrawable.centerRadius = 30f
    circularProgressDrawable.start()
    return circularProgressDrawable
}

fun ImageView.setImageUrls(url: String, screenWidth: Int, screenHeight: Int) {
    Glide.with(this).asBitmap().load(url).placeholder(createCircularProgressDrawable(this.context)).centerCrop().into(object : CustomViewTarget<ImageView, Bitmap>(this) {
        override fun onLoadFailed(errorDrawable: Drawable?) {

        }

        override fun onResourceCleared(placeholder: Drawable?) {

        }

        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            var width = resource.width
            var height = resource.height

            while (width > (screenWidth / 2) && height > (screenHeight / 2)) {
                if (width > (screenWidth / 2)) width /= 2
                if (height > (screenHeight / 2)) height /= 2
            }

            this@setImageUrls.layoutParams?.width = width
            this@setImageUrls.layoutParams?.height = height
        }
    })
}

fun ImageView.setImageUrlFromVideo(url: String) {
    Glide.with(this).load(url).error(ColorDrawable(Color.BLACK)).into(this)
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

    /*val sdf = SimpleDateFormat(Const.DATE_TIME_FORMAT, locale)
    val serverTime = sdf.parse(this)

    val dateTimeMillis = if (this.isNotEmpty()) {
        sdf.parse(this).time
    } else {
        System.currentTimeMillis()
    }

    val dateServer = Date(dateTimeMillis)

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = dateTimeMillis

    return if (dateTimeMillis != 0.toLong()) {
        SimpleDateFormat(format, locale).format(calendar.time)
    } else {
        SimpleDateFormat(format, locale).format(System.currentTimeMillis())
    }*/

    if(this.length < 3){
        var getDate = Date(System.currentTimeMillis())
        return SimpleDateFormat(format).format(getDate)
    }else{
        val inputFormat = SimpleDateFormat(Const.DATE_TIME_FORMAT, locale)
        inputFormat.timeZone = TimeZone.getTimeZone("SG")
        var getDate = inputFormat.parse(this)
        return SimpleDateFormat(format).format(getDate)
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

fun resaveBitmap(imagepath : String, path:String, filename:String, rotation:Float): File {
    var extStorageDirectory = path
    var outStream : OutputStream? = null
    var file = File(imagepath)

    try{
        var bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap = checkRotationFromCamera(bitmap, imagepath, rotation)
        bitmap = Bitmap.createScaledBitmap(bitmap, (bitmap.getWidth() * 0.3f).toInt(),(bitmap.getHeight() * 0.3f).toInt(), false)
        if(file.exists()){
            file.delete()
            file = File(extStorageDirectory, filename)
        }
        outStream = FileOutputStream(file.path)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
        outStream.flush()
        outStream.close()
    }catch (e:Exception){
        e.printStackTrace()
    }

    return file
}

private fun checkRotationFromCamera(bitmap: Bitmap, pathToFile: String, rotate: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotate)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

fun getImageOrientation(imagePath: String? = ""): Int {
    var rotate = 0
    try {
        val exif = ExifInterface(imagePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_270 -> rotate = 270
            ExifInterface.ORIENTATION_ROTATE_180 -> rotate = 180
            ExifInterface.ORIENTATION_ROTATE_90 -> rotate = 90
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return rotate
}