package com.auralis.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AndroidNativeTtsSynthesizer(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private suspend fun getTts(): TextToSpeech? = withContext(Dispatchers.Main) {
        if (tts != null && isInitialized) return@withContext tts

        suspendCancellableCoroutine { continuation ->
            var instance: TextToSpeech? = null
            instance = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    instance?.language = Locale.US
                    tts = instance
                    isInitialized = true
                    if (continuation.isActive) continuation.resume(instance)
                } else {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    suspend fun synthesizeToFile(text: String, outputFile: File): Boolean {
        if (text.isBlank()) return false
        outputFile.parentFile?.mkdirs()
        val engine = withTimeoutOrNull(6000L) { getTts() } ?: return false

        return withContext(Dispatchers.IO) {
            val utteranceId = "tts_${UUID.randomUUID()}"
            val tempWav = File(outputFile.parentFile, "${outputFile.name}.temp.wav")
            if (tempWav.exists()) tempWav.delete()

            val success = withTimeoutOrNull(45_000L) {
                suspendCancellableCoroutine<Boolean> { cont ->
                    val listener = object : UtteranceProgressListener() {
                        override fun onStart(id: String?) {}
                        override fun onDone(id: String?) {
                            if (id == utteranceId && cont.isActive) {
                                cont.resume(true)
                            }
                        }

                        override fun onError(id: String?) {
                            if (id == utteranceId && cont.isActive) {
                                cont.resume(false)
                            }
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(id: String?, errorCode: Int) {
                            if (id == utteranceId && cont.isActive) {
                                cont.resume(false)
                            }
                        }
                    }

                    engine.setOnUtteranceProgressListener(listener)
                    val params = Bundle().apply {
                        putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                        putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f)
                    }

                    val result = engine.synthesizeToFile(text, params, tempWav, utteranceId)
                    if (result != TextToSpeech.SUCCESS) {
                        if (cont.isActive) cont.resume(false)
                    }
                }
            } ?: false

            if (success && tempWav.exists() && tempWav.length() > 44L) {
                if (outputFile.exists()) outputFile.delete()
                val renamed = tempWav.renameTo(outputFile)
                if (!renamed) {
                    tempWav.copyTo(outputFile, overwrite = true)
                    tempWav.delete()
                }
                true
            } else {
                tempWav.delete()
                false
            }
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (_: Throwable) {}
    }
}
