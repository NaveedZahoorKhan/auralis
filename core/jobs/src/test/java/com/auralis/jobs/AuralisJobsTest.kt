package com.auralis.jobs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuralisJobsTest {

    @Test
    fun databaseSyncWorker_constantsVerification() {
        assertEquals("auralis-periodic-db-sync", DatabaseSyncWorker.WORK_NAME_PERIODIC)
        assertEquals("auralis-instant-db-sync", DatabaseSyncWorker.WORK_NAME_ON_DEMAND)
        assertEquals("synced_books_count", DatabaseSyncWorker.KEY_BOOKS_COUNT)
        assertEquals("positions_audited", DatabaseSyncWorker.KEY_POSITIONS_AUDITED)
        assertEquals("orphaned_files_cleaned", DatabaseSyncWorker.KEY_ORPHANED_FILES_CLEANED)
        assertEquals("stale_jobs_reset", DatabaseSyncWorker.KEY_STALE_JOBS_RESET)
        assertEquals("sync_timestamp", DatabaseSyncWorker.KEY_SYNC_TIMESTAMP)
    }

    @Test
    fun backgroundDocumentProcessingWorker_constantsVerification() {
        assertEquals("book_id", BackgroundDocumentProcessingWorker.KEY_BOOK_ID)
        assertEquals("source_uri", BackgroundDocumentProcessingWorker.KEY_SOURCE_URI)
        assertEquals("run_ai_analysis", BackgroundDocumentProcessingWorker.KEY_RUN_AI_ANALYSIS)
        assertEquals("processed_count", BackgroundDocumentProcessingWorker.KEY_PROCESSED_COUNT)
        assertTrue(BackgroundDocumentProcessingWorker.WORK_NAME_PREFIX.startsWith("doc-processing-"))
    }

    @Test
    fun auralisJobsScheduler_constantsVerification() {
        assertEquals(60L, AuralisJobsScheduler.DEFAULT_SYNC_INTERVAL_MINUTES)
        assertEquals(15L, AuralisJobsScheduler.DEFAULT_SYNC_FLEX_MINUTES)
        assertEquals("tag_database_sync", AuralisJobsScheduler.TAG_DATABASE_SYNC)
        assertEquals("tag_document_processing", AuralisJobsScheduler.TAG_DOCUMENT_PROCESSING)
        assertEquals("tag_audiobook_generation", AuralisJobsScheduler.TAG_AUDIOBOOK_GENERATION)
    }

    @Test
    fun audiobookGenerationWorker_constantsVerification() {
        assertEquals("book_id", AudiobookGenerationWorker.KEY_BOOK_ID)
    }
}
