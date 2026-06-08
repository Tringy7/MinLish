package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.ReviewHistoryDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.VocabularySetDao
import com.example.data.local.dao.VocabularyWordDao
import com.example.data.local.entity.*

@Database(
    entities = [
        UserEntity::class,
        VocabularySetEntity::class,
        VocabularyWordEntity::class,
        ReviewHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun reviewHistoryDao(): ReviewHistoryDao
    abstract fun vocabularySetDao(): VocabularySetDao
    abstract fun vocabularyWordDao(): VocabularyWordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minlish.db"
                )
                .fallbackToDestructiveMigration(true)
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
