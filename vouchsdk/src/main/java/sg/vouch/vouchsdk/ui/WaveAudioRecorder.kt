package sg.vouch.vouchsdk.ui

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Base64
import java.io.*

class WaveAudioRecorder(private val externalCachePath: File,
                        private val base64Audio: (String) -> Unit) {

    private var mAudioRecorder: AudioRecord? = null
    private var mRecordingThread: Thread? = null

    var isRecording = false

    private fun getTempFileName(): String {
        val file = File(externalCachePath, AUDIO_RECORDER_TEMP_FILE)

        return file.absolutePath
    }

    private fun getFileName(): String {
        val file = File(externalCachePath, "$AUDIO_RECORDER_TEMP_FILE$AUDIO_RECORDER_FILE_EXT_WAV")

        if (file.exists()) file.delete()

        return file.absolutePath
    }

    fun startRecording() {
        mAudioRecorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING,
            BUFFER_SIZE
        )

        val recordState = mAudioRecorder?.state
        if (recordState == 1) mAudioRecorder?.startRecording()

        isRecording = true

        mRecordingThread = Thread(Runnable { writeAudioDataToFile() },
            "AudioRecorder Thread")

        mRecordingThread?.start()
    }

    fun stopRecording() {
        if (mAudioRecorder != null) {
            isRecording = false

            val recordState = mAudioRecorder?.state
            if (recordState == 1) {
                mAudioRecorder?.stop()
                mAudioRecorder?.release()
            }

            mAudioRecorder = null
            mRecordingThread = null
        }

        copyWaveFile(getTempFileName(), getFileName())
        deleteTempFile()
    }

    private fun deleteTempFile() {
        val file = File(getTempFileName())
        if (file.exists()) file.delete()
    }

    private fun writeAudioDataToFile() {
        val data = ByteArray(BUFFER_SIZE)
        val fileName = getTempFileName()
        var outputStream: FileOutputStream? = null

        try {
            outputStream = FileOutputStream(fileName)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        var read: Int?

        outputStream?.use {
            while (isRecording) {
                read = mAudioRecorder?.read(data, 0, BUFFER_SIZE)

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    it.write(data)
                }
            }
        }
    }

    private fun copyWaveFile(inputFileName: String, outputFileName: String) {
        val inStream: FileInputStream
        val outStream: FileOutputStream
        var totalAudioLen: Long = 0
        var totalDataLen = totalAudioLen + 36
        val longSampleRate = RECORDER_SAMPLE_RATE.toLong()
        val channels = 2
        val byteRate = (RECORDER_BPP * RECORDER_SAMPLE_RATE * channels / 8).toLong()

        val data = ByteArray(BUFFER_SIZE)

        try {
            inStream = FileInputStream(inputFileName)
            outStream = FileOutputStream(outputFileName)
            totalAudioLen = inStream.channel.size()
            totalDataLen = totalAudioLen + 36

            writeWaveHeader(outStream, totalAudioLen, totalDataLen, longSampleRate, channels, byteRate)

            while (inStream.read(data) != -1) {
                outStream.write(data)
            }

            inStream.close()
            outStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        base64Audio(Base64.encodeToString(File(outputFileName).readBytes(), Base64.DEFAULT))
    }

    private fun writeWaveHeader(
        out: FileOutputStream,
        totalAudioLen: Long,
        totalDataLen: Long,
        longSampleRate: Long,
        channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF mark 4 bytes
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte() // file size in 4 bytes
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.toByte() // WAVE mark 4 bytes
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk 4 bytes
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 'fmt ' chunk size
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM format
        header[21] = 0
        header[22] = channels.toByte() // channels = 2
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte() // sample rate in 4 bytes
        header[25] = ((longSampleRate shr 8) and 0xff).toByte()
        header[26] = ((longSampleRate shr 16) and 0xff).toByte()
        header[27] = ((longSampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte() // (sample rate * BPP * channels) / 8
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * RECORDER_BPP / 8).toByte() // BPP(bits per sample) * channels / 8
        header[33] = 0
        header[34] = RECORDER_BPP.toByte() // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte() // data mark
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte() // audio size in 4 bytes
        header[41] = ((totalAudioLen shl 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shl 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shl 24) and 0xff).toByte()

        out.write(header, 0, 44)
    }

    companion object {

        private const val RECORDER_BPP = 16
        private const val AUDIO_RECORDER_FILE_EXT_WAV = ".wav"
        private const val AUDIO_RECORDER_TEMP_FILE = "audio_temp"
        private const val RECORDER_SAMPLE_RATE = 16000
        private const val RECORDER_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO
        private const val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT
        private val BUFFER_SIZE = AudioRecord
            .getMinBufferSize(RECORDER_SAMPLE_RATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)
    }
}