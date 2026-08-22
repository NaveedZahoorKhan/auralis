package com.auralis.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String?,
    val format: String,
    val sourceUri: String,
    val localPath: String,
    val importStatus: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "chapters",
    indices = [Index("bookId"), Index(value = ["bookId", "sortIndex"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = BookEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ChapterEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val title: String,
    val sortIndex: Int,
    val textPath: String,
    val characterCount: Int,
    val pageStart: Int?,
    val pageEnd: Int?
)

@Entity(tableName = "reading_positions")
data class ReadingPositionEntity(
    @PrimaryKey val bookId: String,
    val chapterId: String?,
    val textOffset: Int,
    val pageIndex: Int?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "bookmarks",
    indices = [Index("bookId"), Index("chapterId")]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterId: String,
    val textOffset: Int,
    val label: String,
    val createdAtMillis: Long
)

@Entity(
    tableName = "highlights",
    indices = [Index("bookId"), Index("chapterId")]
)
data class HighlightEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val chapterId: String,
    val startOffset: Int,
    val endOffset: Int,
    val note: String?,
    val colorName: String,
    val createdAtMillis: Long
)

@Entity(tableName = "book_metadata")
data class BookMetadataEntity(
    @PrimaryKey val bookId: String,
    val language: String,
    val genre: String,
    val tone: String,
    val synopsis: String,
    val source: String,
    val confidence: Float,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "characters",
    indices = [Index("bookId")]
)
data class CharacterProfileEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val name: String,
    val aliases: String,
    val description: String,
    val pronunciation: String?,
    val confidence: Float
)

@Entity(
    tableName = "pronunciation_hints",
    indices = [Index("bookId")]
)
data class PronunciationHintEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val phrase: String,
    val hint: String,
    val source: String
)

@Entity(tableName = "voice_models")
data class VoiceModelEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val language: String,
    val runtime: String,
    val status: String,
    val modelPath: String?,
    val configPath: String?,
    val sizeBytes: Long?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "audiobook_jobs",
    indices = [Index("bookId")]
)
data class AudiobookJobEntity(
    @PrimaryKey val id: String,
    val bookId: String,
    val voiceModelId: String?,
    val status: String,
    val currentChapterId: String?,
    val completedSegments: Int,
    val totalSegments: Int,
    val lastError: String?,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "audio_segments",
    indices = [Index("bookId"), Index("chapterId"), Index("jobId")]
)
data class AudioSegmentEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val bookId: String,
    val chapterId: String,
    val sortIndex: Int,
    val textStartOffset: Int,
    val textEndOffset: Int,
    val filePath: String,
    val durationMillis: Long,
    val checksum: String,
    val createdAtMillis: Long
)
