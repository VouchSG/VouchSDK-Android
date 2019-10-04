package id.gits.vouchsdk.utils

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import okio.Source
import okio.source

class InputStreamRequestBody(
    private val contentResolver: ContentResolver?,
    private val uri: Uri
) : RequestBody() {

    override fun contentType(): MediaType? {
        return contentResolver?.getType(uri)?.let { it.toMediaTypeOrNull() }
//        return MediaType.parse("multipart/form-data")
    }

    override fun contentLength(): Long {
        return super.contentLength()
    }

    override fun writeTo(sink: BufferedSink) {
        var source: Source? = null

        try {
            contentResolver?.openInputStream(uri)?.let { source = it.source() }
            source?.let { sink.writeAll(it) }
        } finally {
            sink.close()
        }
    }
}