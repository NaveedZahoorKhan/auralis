package com.auralis.jobs

import android.content.Context
import android.net.Uri
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * Centralized WorkManager scheduling orchestrator for Auralis.
 * Manages:
 * - Periodic local database progress sync and cache pruning.
 * - On-demand background document extraction and AI enrichment.
 * - Background audiobook voice synthesis tasks.
 */
class AuralisJobsScheduler(private val context: Context) {

    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    /**
     * Schedules periodic background synchronization of reading progress, playback positions,
     * cache maintenance, and integrity checks.
     *
     * @param intervalMinutes Interval between periodic sync runs (minimum 15 minutes by WorkManager spec).
     * @param flexIntervalMinutes Flex window in minutes.
     * @param requireBatteryNotLow Whether to only run when the battery is not low.
     */
    fun schedulePeriodicDatabaseSync(
        intervalMinutes: Long = DEFAULT_SYNC_INTERVAL_MINUTES,
        flexIntervalMinutes: Long = DEFAULT_SYNC_FLEX_MINUTES,
        requireBatteryNotLow: Boolean = true
    ) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(requireBatteryNotLow)
            .setRequiresStorageNotLow(true)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<DatabaseSyncWorker>(
            intervalMinutes.coerceAtLeast(15),
            TimeUnit.MINUTES,
            flexIntervalMinutes.coerceAtLeast(5),
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG_DATABASE_SYNC)
            .build()

        workManager.enqueueUniquePeriodicWork(
            DatabaseSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicRequest
        )
    }

    /**
     * Enqueues an immediate, one-time sync and audit of database positions and cache files.
     */
    fun enqueueImmediateDatabaseSync() {
        val request = OneTimeWorkRequestBuilder<DatabaseSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(TAG_DATABASE_SYNC)
            .build()

        workManager.enqueueUniqueWork(
            DatabaseSyncWorker.WORK_NAME_ON_DEMAND,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Enqueues a background document processing task to extract text/chapters
     * and run AI intelligence (character profiles, pronunciation hints).
     */
    fun enqueueBackgroundDocumentProcessing(
        bookId: String? = null,
        sourceUri: Uri? = null,
        runAiAnalysis: Boolean = true
    ) {
        val dataBuilder = androidx.work.Data.Builder()
            .putBoolean(BackgroundDocumentProcessingWorker.KEY_RUN_AI_ANALYSIS, runAiAnalysis)

        bookId?.let { dataBuilder.putString(BackgroundDocumentProcessingWorker.KEY_BOOK_ID, it) }
        sourceUri?.let { dataBuilder.putString(BackgroundDocumentProcessingWorker.KEY_SOURCE_URI, it.toString()) }

        val request = OneTimeWorkRequestBuilder<BackgroundDocumentProcessingWorker>()
            .setInputData(dataBuilder.build())
            .setConstraints(
                Constraints.Builder()
                    .setRequiresStorageNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(TAG_DOCUMENT_PROCESSING)
            .build()

        val uniqueWorkName = if (bookId != null) {
            "${BackgroundDocumentProcessingWorker.WORK_NAME_PREFIX}$bookId"
        } else {
            "${BackgroundDocumentProcessingWorker.WORK_NAME_PREFIX}queue"
        }

        workManager.enqueueUniqueWork(
            uniqueWorkName,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    /**
     * Enqueues offline audiobook TTS rendering for a specific book.
     */
    fun enqueueAudiobookGeneration(bookId: String) {
        val request = OneTimeWorkRequestBuilder<AudiobookGenerationWorker>()
            .setInputData(workDataOf(AudiobookGenerationWorker.KEY_BOOK_ID to bookId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(TAG_AUDIOBOOK_GENERATION)
            .build()

        workManager.enqueueUniqueWork(
            "audiobook-$bookId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    /**
     * Cancels any ongoing or scheduled unique work.
     */
    fun cancelUniqueWork(uniqueWorkName: String) {
        workManager.cancelUniqueWork(uniqueWorkName)
    }

    /**
     * Cancels all jobs belonging to a specific tag.
     */
    fun cancelWorkByTag(tag: String) {
        workManager.cancelAllWorkByTag(tag)
    }

    companion object {
        const val DEFAULT_SYNC_INTERVAL_MINUTES = 60L
        const val DEFAULT_SYNC_FLEX_MINUTES = 15L

        const val TAG_DATABASE_SYNC = "tag_database_sync"
        const val TAG_DOCUMENT_PROCESSING = "tag_document_processing"
        const val TAG_AUDIOBOOK_GENERATION = "tag_audiobook_generation"
    }
}
