package com.auralis.jobs

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.auralis.audio.NarrationPlanner
import com.auralis.audio.OnnxNaturalTtsEngine
import com.auralis.audio.VoiceRuntimeFailure
import com.auralis.database.AudioSegmentEntity
import com.auralis.database.AudiobookJobEntity
import com.auralis.database.AuralisDatabase
import java.io.File
import java.security.MessageDigest
import kotlinx.coroutines.CancellationException

class AudiobookGenerationWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val bookId = inputData.getString(KEY_BOOK_ID) ?: return Result.failure()
        val database = AuralisDatabase.get(applicationContext)
        val dao = database.dao()
        val chapters = dao.getChapters(bookId)
        if (chapters.isEmpty()) {
            upsertJob(bookId, status = "failed", error = "No extracted chapters are available.")
            return Result.failure()
        }

        val voice = dao.getDefaultInstalledVoice()
        if (voice == null) {
            upsertJob(
                bookId = bookId,
                status = "waiting_for_voice",
                totalSegments = chapters.size,
                error = "Install a natural ONNX voice model before generation."
            )
            return Result.success()
        }

        val planner = NarrationPlanner()
        val engine = OnnxNaturalTtsEngine()
        var completed = 0
        val total = chapters.sumOf { chapter ->
            planner.planChapter(chapter.id, File(chapter.textPath).readText()).size.coerceAtLeast(1)
        }

        upsertJob(bookId, voice.id, "running", totalSegments = total)

        return try {
            chapters.forEach { chapter ->
                val text = File(chapter.textPath).readText()
                val outputDir = File(applicationContext.filesDir, "audio/$bookId/${chapter.id}").also { it.mkdirs() }
                planner.planChapter(chapter.id, text).forEach { request ->
                    val rendered = engine.render(request, voice, outputDir.absolutePath)
                    dao.insertAudioSegment(
                        AudioSegmentEntity(
                            id = request.id,
                            jobId = "job-$bookId",
                            bookId = bookId,
                            chapterId = chapter.id,
                            sortIndex = request.sortIndex,
                            textStartOffset = request.textStartOffset,
                            textEndOffset = request.textEndOffset,
                            filePath = rendered.filePath,
                            durationMillis = rendered.durationMillis,
                            checksum = rendered.checksum.ifBlank { checksum(File(rendered.filePath)) },
                            createdAtMillis = System.currentTimeMillis()
                        )
                    )
                    completed += 1
                    upsertJob(bookId, voice.id, "running", chapter.id, completed, total)
                }
            }
            upsertJob(bookId, voice.id, "complete", completedSegments = completed, totalSegments = total)
            Result.success()
        } catch (failure: VoiceRuntimeFailure) {
            val status = when (failure) {
                is VoiceRuntimeFailure.MissingVoiceModel -> "waiting_for_voice"
                is VoiceRuntimeFailure.UnsupportedVoicePack -> "unsupported_voice_pack"
                is VoiceRuntimeFailure.SynthesisFailed -> "failed"
            }
            upsertJob(bookId, voice.id, status, completedSegments = completed, totalSegments = total, error = failure.message)
            Result.success()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (throwable: Throwable) {
            upsertJob(bookId, voice.id, "failed", completedSegments = completed, totalSegments = total, error = throwable.message)
            Result.retry()
        }
    }

    private suspend fun upsertJob(
        bookId: String,
        voiceModelId: String? = null,
        status: String,
        currentChapterId: String? = null,
        completedSegments: Int = 0,
        totalSegments: Int = 0,
        error: String? = null
    ) {
        AuralisDatabase.get(applicationContext).dao().upsertAudiobookJob(
            AudiobookJobEntity(
                id = "job-$bookId",
                bookId = bookId,
                voiceModelId = voiceModelId,
                status = status,
                currentChapterId = currentChapterId,
                completedSegments = completedSegments,
                totalSegments = totalSegments,
                lastError = error,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    private fun checksum(file: File): String {
        if (!file.exists()) return ""
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

    companion object {
        const val KEY_BOOK_ID = "book_id"
    }
}
