package com.auralis.audio

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.auralis.database.AuralisDao
import com.auralis.database.VoiceModelEntity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class VoiceModelRepository(
    private val context: Context,
    private val dao: AuralisDao
) {
    suspend fun seedCatalog() {
        val existing = dao.getVoiceModel(DEFAULT_KOKORO_VOICE_ID)
        installedVoiceFromFiles()?.let {
            if (existing?.status != "installed" || existing.modelPath != it.modelPath) {
                dao.upsertVoiceModel(it)
            }
            return
        }

        val now = System.currentTimeMillis()
        dao.upsertVoiceModel(
            VoiceModelEntity(
                id = DEFAULT_KOKORO_VOICE_ID,
                displayName = "Kokoro Natural English",
                language = "en",
                runtime = "kokoro-onnx",
                status = "installed",
                modelPath = null,
                configPath = null,
                sizeBytes = 85_000_000L,
                updatedAtMillis = now
            )
        )
    }

    suspend fun installOnnxVoice(uri: Uri, displayName: String? = null): VoiceModelEntity {
        val voiceDir = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID").also { it.mkdirs() }
        val fileName = displayName ?: queryDisplayName(uri) ?: "kokoro.onnx"
        val modelFile = File(voiceDir, fileName.ensureOnnxExtension())
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Unable to open selected voice model." }
            modelFile.outputStream().use { output -> input.copyTo(output) }
        }

        val entity = VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "installed",
            modelPath = modelFile.absolutePath,
            configPath = null,
            sizeBytes = modelFile.length(),
            updatedAtMillis = System.currentTimeMillis()
        )
        dao.upsertVoiceModel(entity)
        return entity
    }

    suspend fun downloadDefaultKokoroVoice(): VoiceModelEntity {
        val voiceDir = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID").also { it.mkdirs() }
        val now = System.currentTimeMillis()
        val downloadingEntity = VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "downloading",
            modelPath = null,
            configPath = voiceDir.absolutePath,
            sizeBytes = null,
            updatedAtMillis = now
        )
        dao.upsertVoiceModel(downloadingEntity)

        return try {
            val modelFile = downloadFile(
                url = KOKORO_MODEL_URL,
                outputFile = File(voiceDir, KOKORO_MODEL_FILE),
                expectedBytes = KOKORO_MODEL_BYTES
            )
            downloadFile(
                url = KOKORO_CONFIG_URL,
                outputFile = File(voiceDir, "config.json"),
                expectedBytes = KOKORO_CONFIG_BYTES
            )
            downloadFile(
                url = KOKORO_VOICE_URL,
                outputFile = File(voiceDir, "af.bin"),
                expectedBytes = KOKORO_VOICE_BYTES
            )

            val entity = installedVoiceFromFiles(modelFile.parentFile ?: voiceDir)
                ?: error("Downloaded voice files are incomplete.")
            dao.upsertVoiceModel(entity)
            entity
        } catch (throwable: Throwable) {
            dao.upsertVoiceModel(downloadingEntity.copy(status = "available", updatedAtMillis = System.currentTimeMillis()))
            throw throwable
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    private fun String.ensureOnnxExtension(): String {
        return if (endsWith(".onnx", ignoreCase = true)) this else "$this.onnx"
    }

    private fun installedVoiceFromFiles(voiceDir: File = File(context.filesDir, "voices/$DEFAULT_KOKORO_VOICE_ID")): VoiceModelEntity? {
        val modelFile = File(voiceDir, "model_q8f16.onnx")
        val quantizedModelFile = File(voiceDir, KOKORO_MODEL_FILE)
        val configFile = File(voiceDir, "config.json")
        val voiceFile = File(voiceDir, "af.bin")
        val selectedModelFile = when {
            quantizedModelFile.length() >= KOKORO_MODEL_BYTES -> quantizedModelFile
            modelFile.length() >= LEGACY_KOKORO_Q8F16_MODEL_BYTES -> null
            else -> null
        } ?: return null
        if (configFile.length() < KOKORO_CONFIG_BYTES) return null
        if (voiceFile.length() < KOKORO_VOICE_BYTES) return null
        return VoiceModelEntity(
            id = DEFAULT_KOKORO_VOICE_ID,
            displayName = "Kokoro Natural English",
            language = "en",
            runtime = "kokoro-onnx",
            status = "installed",
            modelPath = selectedModelFile.absolutePath,
            configPath = voiceDir.absolutePath,
            sizeBytes = voiceDir.walkTopDown().filter { it.isFile }.sumOf { it.length() },
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    private fun downloadFile(url: String, outputFile: File, expectedBytes: Long): File {
        if (outputFile.exists() && outputFile.length() >= expectedBytes) {
            return outputFile
        }

        val tempFile = File(outputFile.parentFile, "${outputFile.name}.download")

        repeat(DOWNLOAD_ATTEMPTS) { attempt ->
            var connection: HttpURLConnection? = null
            try {
                val existingBytes = tempFile.length().coerceAtMost(expectedBytes)
                connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15_000
                    readTimeout = 20_000
                    instanceFollowRedirects = true
                    if (existingBytes > 0L) {
                        setRequestProperty("Range", "bytes=$existingBytes-")
                    }
                }
                val responseCode = connection.responseCode
                val append = existingBytes > 0L && responseCode == HttpURLConnection.HTTP_PARTIAL
                if (!append && tempFile.exists()) tempFile.delete()

                connection.inputStream.use { input ->
                    FileOutputStream(tempFile, append).use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.length() >= expectedBytes) {
                    if (outputFile.exists()) outputFile.delete()
                    check(tempFile.renameTo(outputFile)) { "Unable to save ${outputFile.name}" }
                    check(outputFile.length() >= expectedBytes) { "Downloaded ${outputFile.name} is incomplete." }
                    return outputFile
                }
            } catch (exception: IOException) {
                if (attempt == DOWNLOAD_ATTEMPTS - 1) throw exception
            } finally {
                connection?.disconnect()
            }
        }

        error("Downloaded ${outputFile.name} is incomplete.")
    }

    companion object {
        const val DEFAULT_KOKORO_VOICE_ID = "kokoro-natural-en"
        private const val DOWNLOAD_ATTEMPTS = 5
        private const val KOKORO_MODEL_FILE = "model_quantized.onnx"
        private const val KOKORO_MODEL_BYTES = 92_361_116L
        private const val LEGACY_KOKORO_Q8F16_MODEL_BYTES = 86_033_585L
        private const val KOKORO_CONFIG_BYTES = 44L
        private const val KOKORO_VOICE_BYTES = 522_240L
        private const val KOKORO_BASE_URL = "https://huggingface.co/onnx-community/Kokoro-82M-v1.0-ONNX/resolve/main"
        private const val KOKORO_MODEL_URL = "$KOKORO_BASE_URL/onnx/$KOKORO_MODEL_FILE"
        private const val KOKORO_CONFIG_URL = "$KOKORO_BASE_URL/config.json"
        private const val KOKORO_VOICE_URL = "$KOKORO_BASE_URL/voices/af_heart.bin"
    }
}
