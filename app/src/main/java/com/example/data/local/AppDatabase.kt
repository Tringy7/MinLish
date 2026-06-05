package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
    version = 8,
    exportSchema = false
)
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
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minlish_database"
                ).createFromAsset("database/minlish_seed.db") // Temporarily commented out until the asset file is provided
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
