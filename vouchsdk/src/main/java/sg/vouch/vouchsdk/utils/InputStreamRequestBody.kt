package sg.vouch.vouchsdk.utils

import android.content.ContentResolver
import android.net.Uri
import sg.vouch.vouchsdk.BuildConfig
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Source
import okio.source

class InputStreamRequestBody(
    private val contentResolver: ContentResolver?,
    private val uri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        return contentResolver?.getType(uri)?.toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null

        try {
            contentResolver?.openInputStream(uri)?.let { source = it.source() }
            source?.let { sink.writeAll(it) }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) e.printStackTrace()
        } finally {
            sink.close()
        }
    }
}