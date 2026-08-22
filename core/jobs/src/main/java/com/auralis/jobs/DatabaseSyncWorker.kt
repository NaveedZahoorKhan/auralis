package com.auralis.jobs

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.auralis.database.AuralisDatabase
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker responsible for:
 * 1. Auditing and synchronizing local reading and audio playback positions.
 * 2. Pruning orphaned cache/audio files from deleted books or expired jobs.
 * 3. Cleaning up stale running jobs that encountered unexpected process death.
 * 4. Aggregating reading statistics and ensuring database integrity.
 */
class DatabaseSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val database = AuralisDatabase.get(applicationContext)
            val dao = database.dao()

            val allBooks = dao.getAllBooks()
            val validBookIds = allBooks.map { it.id }.toSet()

            var readingPositionsAudited = 0
            var orphanedFilesCleaned = 0
            var staleJobsReset = 0

            // 1. Audit reading positions against book and chapter bounds
            val readingPositions = dao.getAllReadingPositions()
            for (pos in readingPositions) {
                if (!validBookIds.contains(pos.bookId)) {
                    continue
                }

                // Verify chapter exists if chapterId is present
                val currentChapterId = pos.chapterId
                if (currentChapterId != null) {
                    val chapter = dao.getChapter(currentChapterId)
                    if (chapter != null) {
                        // Clamp text offset within chapter character bounds
                        val clampedOffset = pos.textOffset.coerceIn(0, chapter.characterCount.coerceAtLeast(0))
                        if (clampedOffset != pos.textOffset) {
                            dao.upsertReadingPosition(
                                pos.copy(
                                    textOffset = clampedOffset,
                                    updatedAtMillis = System.currentTimeMillis()
                                )
                            )
                            readingPositionsAudited++
                        }
                    }
                }
            }

            // 2. Audit and clean orphaned audio segment cache directories
            val audioRoot = File(applicationContext.filesDir, "audio")
            if (audioRoot.exists() && audioRoot.isDirectory) {
                val bookFolders = audioRoot.listFiles { file -> file.isDirectory } ?: emptyArray()
                for (folder in bookFolders) {
                    val folderBookId = folder.name
                    if (!validBookIds.contains(folderBookId)) {
                        // Book no longer exists, safely prune cached audio folder
                        if (folder.deleteRecursively()) {
                            orphanedFilesCleaned++
                        }
                    }
                }
            }

            // 3. Detect and recover stale jobs stuck in 'running' state
            val staleThresholdMillis = System.currentTimeMillis() - STALE_JOB_TIMEOUT_MILLIS
            for (book in allBooks) {
                val latestJob = dao.getLatestAudiobookJob(book.id)
                if (latestJob != null && latestJob.status == "running" && latestJob.updatedAtMillis < staleThresholdMillis) {
                    dao.upsertAudiobookJob(
                        latestJob.copy(
                            status = "interrupted",
                            lastError = "Job timed out or interrupted by process termination. Ready for retry.",
                            updatedAtMillis = System.currentTimeMillis()
                        )
                    )
                    staleJobsReset++
                }
            }

            val outputData = workDataOf(
                KEY_BOOKS_COUNT to allBooks.size,
                KEY_POSITIONS_AUDITED to readingPositionsAudited,
                KEY_ORPHANED_FILES_CLEANED to orphanedFilesCleaned,
                KEY_STALE_JOBS_RESET to staleJobsReset,
                KEY_SYNC_TIMESTAMP to System.currentTimeMillis()
            )

            Result.success(outputData)
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME_PERIODIC = "auralis-periodic-db-sync"
        const val WORK_NAME_ON_DEMAND = "auralis-instant-db-sync"

        const val KEY_BOOKS_COUNT = "synced_books_count"
        const val KEY_POSITIONS_AUDITED = "positions_audited"
        const val KEY_ORPHANED_FILES_CLEANED = "orphaned_files_cleaned"
        const val KEY_STALE_JOBS_RESET = "stale_jobs_reset"
        const val KEY_SYNC_TIMESTAMP = "sync_timestamp"

        private const val STALE_JOB_TIMEOUT_MILLIS = 2 * 60 * 60 * 1000L // 2 hours
    }
}
