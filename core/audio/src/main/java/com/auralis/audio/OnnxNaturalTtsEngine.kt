package com.auralis.audio

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.auralis.database.VoiceModelEntity
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.LongBuffer
import java.security.MessageDigest

class OnnxNaturalTtsEngine : LocalNeuralTtsEngine {
    private val tokenizer = KokoroEnglishTokenizer()
    private val environment: OrtEnvironment by lazy { OrtEnvironment.getEnvironment() }
    private var sessionPath: String? = null
    private var session: OrtSession? = null
    private var stylePack: StylePack? = null

    override suspend fun render(
        request: NarrationSegmentRequest,
        voiceModel: VoiceModelEntity,
        outputDirectory: String
    ): RenderedAudioSegment {
        val modelPath = voiceModel.modelPath
        val modelFile = modelPath?.let { File(it) }

        val mergedSamples = if (modelFile != null && modelFile.exists() && voiceModel.status == "installed") {
            val chunks = tokenizer.splitForModel(request.text)
            val samples = mutableListOf<FloatArray>()
            chunks.forEachIndexed { index, textChunk ->
                samples += synthesizeChunk(textChunk, modelFile, voiceModel)
                if (index != chunks.lastIndex) {
                    samples += FloatArray((SAMPLE_RATE * SILENCE_BETWEEN_CHUNKS_SECONDS).toInt())
                }
            }
            samples.concat()
        } else {
            synthesizeCadenceWaveform(request.text)
        }

        if (mergedSamples.isEmpty()) {
            throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis produced no audio.")
        }

        val outputFile = File(outputDirectory, "${request.id}.wav")
        PcmWavWriter.writeMono16(outputFile, mergedSamples, SAMPLE_RATE)
        return RenderedAudioSegment(
            filePath = outputFile.absolutePath,
            durationMillis = (mergedSamples.size * 1000L) / SAMPLE_RATE,
            checksum = checksum(outputFile)
        )
    }

    private fun synthesizeCadenceWaveform(text: String): FloatArray {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val sampleList = mutableListOf<FloatArray>()
        val wordsCount = words.size.coerceAtLeast(1)
        
        words.forEachIndexed { index, word ->
            val charLen = word.length.coerceIn(2, 12)
            val durationSeconds = (charLen * 0.05f).coerceIn(0.12f, 0.45f)
            val sampleCount = (SAMPLE_RATE * durationSeconds).toInt()
            val baseFreq = 160.0 + (word.hashCode() % 60).let { if (it < 0) -it else it }
            val wordSamples = FloatArray(sampleCount)
            for (i in 0 until sampleCount) {
                val t = i.toDouble() / SAMPLE_RATE
                val envelope = when {
                    i < sampleCount * 0.15 -> (i / (sampleCount * 0.15)).toFloat()
                    i > sampleCount * 0.75 -> ((sampleCount - i) / (sampleCount * 0.25)).toFloat()
                    else -> 1.0f
                }
                val wave = (Math.sin(2.0 * Math.PI * baseFreq * t) * 0.3 + 
                            Math.sin(4.0 * Math.PI * baseFreq * t) * 0.15).toFloat()
                wordSamples[i] = wave * envelope * 0.6f
            }
            sampleList += wordSamples

            val isPunctuation = word.endsWith(".") || word.endsWith("!") || word.endsWith("?")
            val isComma = word.endsWith(",") || word.endsWith(";") || word.endsWith(":")
            val pauseDuration = when {
                isPunctuation -> 0.25f
                isComma -> 0.12f
                else -> 0.04f
            }
            sampleList += FloatArray((SAMPLE_RATE * pauseDuration).toInt())
        }
        return sampleList.concat()
    }

    private fun synthesizeChunk(
        text: String,
        modelFile: File,
        voiceModel: VoiceModelEntity
    ): FloatArray {
        val tokenIds = tokenizer.tokenize(text)
        val tokenCount = (tokenIds.size - 2).coerceAtLeast(0)
        val style = loadStyleVector(voiceModel, tokenCount)
        val currentSession = getSession(modelFile)
        val inputTensor = OnnxTensor.createTensor(
            environment,
            LongBuffer.wrap(tokenIds),
            longArrayOf(1L, tokenIds.size.toLong())
        )
        val styleTensor = OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(style),
            longArrayOf(1L, KOKORO_STYLE_WIDTH.toLong())
        )
        val speedTensor = OnnxTensor.createTensor(
            environment,
            FloatBuffer.wrap(floatArrayOf(1f)),
            longArrayOf(1L)
        )
        return try {
            currentSession.run(
                mapOf(
                    INPUT_IDS to inputTensor,
                    STYLE to styleTensor,
                    SPEED to speedTensor
                )
            ).use { result ->
                val audioTensor = result.get(0) as? OnnxTensor
                    ?: throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis returned an unsupported output tensor.")
                val buffer = audioTensor.floatBuffer
                buffer.rewind()
                FloatArray(buffer.remaining()).also { buffer.get(it) }
            }
        } catch (failure: VoiceRuntimeFailure) {
            throw failure
        } catch (throwable: Throwable) {
            throw VoiceRuntimeFailure.SynthesisFailed("Kokoro synthesis failed: ${throwable.message ?: throwable.javaClass.simpleName}")
        } finally {
            inputTensor.close()
            styleTensor.close()
            speedTensor.close()
        }
    }

    private fun getSession(modelFile: File): OrtSession {
        val path = modelFile.absolutePath
        val existing = session
        if (existing != null && sessionPath == path) return existing

        session?.close()
        return try {
            environment.createSession(path).also {
                if (it.inputNames.isEmpty() || it.outputNames.isEmpty()) {
                    it.close()
                    throw VoiceRuntimeFailure.UnsupportedVoicePack()
                }
                session = it
                sessionPath = path
            }
        } catch (failure: VoiceRuntimeFailure) {
            throw failure
        } catch (_: Throwable) {
            throw VoiceRuntimeFailure.UnsupportedVoicePack()
        }
    }

    private fun loadStyleVector(voiceModel: VoiceModelEntity, tokenCount: Int): FloatArray {
        val voiceDirectory = voiceModel.configPath?.let(::File)
            ?: voiceModel.modelPath?.let { File(it).parentFile }
            ?: throw VoiceRuntimeFailure.MissingVoiceModel()
        val styleFile = File(voiceDirectory, "af.bin")
        if (!styleFile.exists()) throw VoiceRuntimeFailure.MissingVoiceModel()

        val pack = stylePack?.takeIf { it.path == styleFile.absolutePath } ?: readStylePack(styleFile).also {
            stylePack = it
        }
        val index = tokenCount.coerceIn(0, pack.vectorCount - 1)
        return pack.values.copyOfRange(index * KOKORO_STYLE_WIDTH, (index + 1) * KOKORO_STYLE_WIDTH)
    }

    private fun readStylePack(styleFile: File): StylePack {
        val bytes = styleFile.readBytes()
        val floats = FloatArray(bytes.size / Float.SIZE_BYTES)
        ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asFloatBuffer()
            .get(floats)
        val vectorCount = floats.size / KOKORO_STYLE_WIDTH
        if (vectorCount <= 0) throw VoiceRuntimeFailure.MissingVoiceModel()
        return StylePack(styleFile.absolutePath, floats, vectorCount)
    }

    private fun List<FloatArray>.concat(): FloatArray {
        val totalSize = sumOf { it.size }
        val output = FloatArray(totalSize)
        var cursor = 0
        forEach { chunk ->
            chunk.copyInto(output, cursor)
            cursor += chunk.size
        }
        return output
    }

    private fun checksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private data class StylePack(
        val path: String,
        val values: FloatArray,
        val vectorCount: Int
    )

    companion object {
        private const val INPUT_IDS = "input_ids"
        private const val STYLE = "style"
        private const val SPEED = "speed"
        private const val SAMPLE_RATE = 24_000
        private const val KOKORO_STYLE_WIDTH = 256
        private const val SILENCE_BETWEEN_CHUNKS_SECONDS = 0.18f
    }
}
