package com.auralis.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AuralisDao {
    @Query("SELECT * FROM books ORDER BY updatedAtMillis DESC")
    fun observeBooks(): Flow<List<BookEntity>>

    @Query("SELECT * FROM books ORDER BY updatedAtMillis DESC")
    suspend fun getAllBooks(): List<BookEntity>

    @Query("SELECT * FROM books WHERE importStatus = :status")
    suspend fun getBooksByStatus(status: String): List<BookEntity>

    @Query("SELECT * FROM books WHERE id = :bookId")
    fun observeBook(bookId: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE id = :bookId")
    suspend fun getBook(bookId: String): BookEntity?

    @Upsert
    suspend fun upsertBook(book: BookEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY sortIndex")
    fun observeChapters(bookId: String): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM chapters WHERE bookId = :bookId ORDER BY sortIndex")
    suspend fun getChapters(bookId: String): List<ChapterEntity>

    @Query("SELECT * FROM chapters WHERE id = :chapterId")
    suspend fun getChapter(chapterId: String): ChapterEntity?

    @Upsert
    suspend fun upsertReadingPosition(position: ReadingPositionEntity)

    @Query("SELECT * FROM reading_positions WHERE bookId = :bookId")
    fun observeReadingPosition(bookId: String): Flow<ReadingPositionEntity?>

    @Query("SELECT * FROM reading_positions WHERE bookId = :bookId")
    suspend fun getReadingPosition(bookId: String): ReadingPositionEntity?

    @Query("SELECT * FROM reading_positions")
    suspend fun getAllReadingPositions(): List<ReadingPositionEntity>

    @Insert
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY createdAtMillis DESC")
    fun observeBookmarks(bookId: String): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId AND type = 'audio' ORDER BY segmentIndex ASC, audioTimestampMillis ASC")
    fun observeAudioBookmarks(bookId: String): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks WHERE id = :bookmarkId")
    suspend fun deleteBookmark(bookmarkId: Long)

    @Insert
    suspend fun insertHighlight(highlight: HighlightEntity)

    @Query("SELECT * FROM highlights WHERE bookId = :bookId ORDER BY createdAtMillis DESC")
    fun observeHighlights(bookId: String): Flow<List<HighlightEntity>>

    @Upsert
    suspend fun upsertMetadata(metadata: BookMetadataEntity)

    @Query("SELECT * FROM book_metadata WHERE bookId = :bookId")
    fun observeMetadata(bookId: String): Flow<BookMetadataEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterProfileEntity>)

    @Query("SELECT * FROM characters WHERE bookId = :bookId ORDER BY confidence DESC, name")
    fun observeCharacters(bookId: String): Flow<List<CharacterProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPronunciationHints(hints: List<PronunciationHintEntity>)

    @Query("SELECT * FROM pronunciation_hints WHERE bookId = :bookId ORDER BY phrase")
    fun observePronunciationHints(bookId: String): Flow<List<PronunciationHintEntity>>

    @Upsert
    suspend fun upsertVoiceModel(voiceModel: VoiceModelEntity)

    @Query("SELECT * FROM voice_models ORDER BY displayName")
    fun observeVoiceModels(): Flow<List<VoiceModelEntity>>

    @Query("SELECT * FROM voice_models WHERE status = 'installed' ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun getDefaultInstalledVoice(): VoiceModelEntity?

    @Query("SELECT * FROM voice_models WHERE id = :voiceModelId")
    suspend fun getVoiceModel(voiceModelId: String): VoiceModelEntity?

    @Upsert
    suspend fun upsertAudiobookJob(job: AudiobookJobEntity)

    @Query("SELECT * FROM audiobook_jobs WHERE bookId = :bookId ORDER BY updatedAtMillis DESC LIMIT 1")
    fun observeLatestAudiobookJob(bookId: String): Flow<AudiobookJobEntity?>

    @Query("SELECT * FROM audiobook_jobs WHERE bookId = :bookId ORDER BY updatedAtMillis DESC LIMIT 1")
    suspend fun getLatestAudiobookJob(bookId: String): AudiobookJobEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudioSegment(segment: AudioSegmentEntity)

    @Query("SELECT * FROM audio_segments WHERE bookId = :bookId ORDER BY sortIndex")
    fun observeAudioSegments(bookId: String): Flow<List<AudioSegmentEntity>>

    @Upsert
    suspend fun upsertAudioPlaybackPosition(position: AudioPlaybackPositionEntity)

    @Query("SELECT * FROM audio_playback_positions WHERE bookId = :bookId")
    fun observeAudioPlaybackPosition(bookId: String): Flow<AudioPlaybackPositionEntity?>

    @Query("SELECT * FROM audio_playback_positions WHERE bookId = :bookId")
    suspend fun getAudioPlaybackPosition(bookId: String): AudioPlaybackPositionEntity?

    @Query("DELETE FROM audio_playback_positions WHERE bookId = :bookId")
    suspend fun deleteAudioPlaybackPosition(bookId: String)

    @Query("DELETE FROM books WHERE id = :bookId")
    suspend fun deleteBookEntity(bookId: String)

    @Query("DELETE FROM chapters WHERE bookId = :bookId")
    suspend fun deleteChapters(bookId: String)

    @Query("DELETE FROM reading_positions WHERE bookId = :bookId")
    suspend fun deleteReadingPosition(bookId: String)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteBookmarks(bookId: String)

    @Query("DELETE FROM highlights WHERE bookId = :bookId")
    suspend fun deleteHighlights(bookId: String)

    @Query("DELETE FROM book_metadata WHERE bookId = :bookId")
    suspend fun deleteMetadata(bookId: String)

    @Query("DELETE FROM characters WHERE bookId = :bookId")
    suspend fun deleteCharacters(bookId: String)

    @Query("DELETE FROM pronunciation_hints WHERE bookId = :bookId")
    suspend fun deletePronunciationHints(bookId: String)

    @Query("DELETE FROM audiobook_jobs WHERE bookId = :bookId")
    suspend fun deleteAudiobookJobs(bookId: String)

    @Query("DELETE FROM audio_segments WHERE bookId = :bookId")
    suspend fun deleteAudioSegments(bookId: String)

    @Transaction
    suspend fun deleteBookData(bookId: String) {
        deleteBookEntity(bookId)
        deleteChapters(bookId)
        deleteReadingPosition(bookId)
        deleteBookmarks(bookId)
        deleteHighlights(bookId)
        deleteMetadata(bookId)
        deleteCharacters(bookId)
        deletePronunciationHints(bookId)
        deleteAudioPlaybackPosition(bookId)
        deleteAudioSegments(bookId)
    }

    @Transaction
    suspend fun insertImportedBook(
        book: BookEntity,
        chapters: List<ChapterEntity>,
        metadata: BookMetadataEntity,
        characters: List<CharacterProfileEntity>,
        hints: List<PronunciationHintEntity>,
        job: AudiobookJobEntity
    ) {
        upsertBook(book)
        insertChapters(chapters)
        upsertMetadata(metadata)
        insertCharacters(characters)
        insertPronunciationHints(hints)
        upsertAudiobookJob(job)
    }
}
