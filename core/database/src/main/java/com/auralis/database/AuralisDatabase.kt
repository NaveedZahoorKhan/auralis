package com.auralis.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        ReadingPositionEntity::class,
        BookmarkEntity::class,
        HighlightEntity::class,
        BookMetadataEntity::class,
        CharacterProfileEntity::class,
        PronunciationHintEntity::class,
        VoiceModelEntity::class,
        AudiobookJobEntity::class,
        AudioSegmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AuralisDatabase : RoomDatabase() {
    abstract fun dao(): AuralisDao

    companion object {
        @Volatile
        private var instance: AuralisDatabase? = null

        fun get(context: Context): AuralisDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AuralisDatabase::class.java,
                    "auralis.db"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
