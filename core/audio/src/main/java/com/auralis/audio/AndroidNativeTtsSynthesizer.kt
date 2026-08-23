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
        val cadences = HumanSpeechPacer.analyze(text)
        if (cadences.isEmpty()) return false
        outputFile.parentFile?.mkdirs()
        val engine = withTimeoutOrNull(6000L) { getTts() } ?: return false

        return withContext(Dispatchers.IO) {
            val tempWavs = mutableListOf<File>()
            var overallSuccess = true

            for ((index, cadence) in cadences.withIndex()) {
                val utteranceId = "tts_${UUID.randomUUID()}_$index"
                val sentenceWav = File(outputFile.parentFile, "${outputFile.name}.part_$index.wav")
                if (sentenceWav.exists()) sentenceWav.delete()

                val success = withTimeoutOrNull(15_000L) {
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
                        engine.setPitch(cadence.pitch)
                        engine.setSpeechRate(cadence.speechRate)

                        val params = Bundle().apply {
                            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
                            putFloat(TextToSpeech.Engine.KEY_PARAM_PAN, 0.0f)
                        }

                        val result = engine.synthesizeToFile(cadence.text, params, sentenceWav, utteranceId)
                        if (result != TextToSpeech.SUCCESS) {
                            if (cont.isActive) cont.resume(false)
                        }
                    }
                } ?: false

                if (success && sentenceWav.exists() && sentenceWav.length() > 44L) {
                    tempWavs.add(sentenceWav)
                } else {
                    overallSuccess = false
                    sentenceWav.delete()
                    break
                }
            }

            if (overallSuccess && tempWavs.isNotEmpty()) {
                val combinedSuccess = mergeWavFilesWithPause(tempWavs, cadences, outputFile)
                tempWavs.forEach { it.delete() }
                combinedSuccess
            } else {
                tempWavs.forEach { it.delete() }
                false
            }
        }
    }

    private fun mergeWavFilesWithPause(
        wavFiles: List<File>,
        cadences: List<SentenceCadence>,
        outputFile: File
    ): Boolean {
        return try {
            val samples = mutableListOf<FloatArray>()
            var sampleRate = 24_000

            wavFiles.forEachIndexed { i, wav ->
                val bytes = wav.readBytes()
                if (bytes.size > 44) {
                    // Extract sample rate from WAV header (bytes 24..27)
                    val sr = (bytes[24].toInt() and 0xFF) or
                            ((bytes[25].toInt() and 0xFF) shl 8) or
                            ((bytes[26].toInt() and 0xFF) shl 16) or
                            ((bytes[27].toInt() and 0xFF) shl 24)
                    if (sr in 8000..48000) sampleRate = sr

                    val dataBytes = bytes.copyOfRange(44, bytes.size)
                    val floatSamples = FloatArray(dataBytes.size / 2)
                    for (j in floatSamples.indices) {
                        val low = dataBytes[j * 2].toInt() and 0xFF
                        val high = dataBytes[j * 2 + 1].toInt()
                        val sampleShort = (high shl 8) or low
                        floatSamples[j] = sampleShort / 32768.0f
                    }
                    samples.add(floatSamples)

                    val pauseMs = cadences.getOrNull(i)?.pauseAfterMillis ?: 350L
                    val pauseSamplesCount = ((sampleRate * pauseMs) / 1000L).toInt()
                    samples.add(FloatArray(pauseSamplesCount) { 0f })
                }
            }

            val totalCount = samples.sumOf { it.size }
            val merged = FloatArray(totalCount)
            var cursor = 0
            samples.forEach { chunk ->
                chunk.copyInto(merged, cursor)
                cursor += chunk.size
            }

            if (outputFile.exists()) outputFile.delete()
            PcmWavWriter.writeMono16(outputFile, merged, sampleRate)
            true
        } catch (_: Throwable) {
            false
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
