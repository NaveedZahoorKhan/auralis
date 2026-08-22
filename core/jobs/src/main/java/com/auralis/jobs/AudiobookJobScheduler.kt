package com.auralis.jobs

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AudiobookJobScheduler(private val context: Context) {
    fun enqueue(bookId: String) {
        val request = OneTimeWorkRequestBuilder<AudiobookGenerationWorker>()
            .setInputData(workDataOf(AudiobookGenerationWorker.KEY_BOOK_ID to bookId))
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "audiobook-$bookId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
