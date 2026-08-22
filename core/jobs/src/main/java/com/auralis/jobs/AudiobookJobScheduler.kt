package com.auralis.jobs

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class AudiobookJobScheduler(private val context: Context) {
    private val delegate = AuralisJobsScheduler(context)

    fun enqueue(bookId: String) {
        delegate.enqueueAudiobookGeneration(bookId)
    }
}
