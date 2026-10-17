package com.mustafakoceerr.justrelax.core.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/** Records the microphone as mono AAC at 48 kHz into `cache/record/recording.m4a`. */
internal class AndroidAudioRecorder(private val context: Context) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private val file get() = File(File(context.cacheDir, RECORD_DIRECTORY).apply { mkdirs() }, RECORD_FILE)

    override fun start(): String {
        cancel()
        val output = file
        val recorder = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder())
        try {
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setAudioSamplingRate(SAMPLE_RATE)
            recorder.setAudioEncodingBitRate(BIT_RATE)
            recorder.setAudioChannels(1)
            recorder.setOutputFile(output.path)
            recorder.prepare()
            recorder.start()
        } catch (e: Exception) {
            recorder.release()
            output.delete()
            throw MediaException("Cannot start recording", e)
        }
        this.recorder = recorder
        return output.path
    }

    override fun level(): Float = (recorder?.maxAmplitude ?: 0) / MAX_AMPLITUDE

    override fun stop() {
        val recorder = recorder ?: throw MediaException("Not recording")
        this.recorder = null
        try {
            recorder.stop()
        } catch (e: RuntimeException) {
            // MediaRecorder throws when stopped before it received any audio.
            file.delete()
            throw MediaException("Nothing was recorded", e)
        } finally {
            recorder.release()
        }
    }

    override fun cancel() {
        recorder?.let {
            runCatching { it.stop() }
            it.release()
        }
        recorder = null
        file.delete()
    }

    private companion object {
        const val RECORD_DIRECTORY = "record"
        const val RECORD_FILE = "recording.m4a"
        const val SAMPLE_RATE = 48_000
        const val BIT_RATE = 128_000
        const val MAX_AMPLITUDE = 32_767f
    }
}
