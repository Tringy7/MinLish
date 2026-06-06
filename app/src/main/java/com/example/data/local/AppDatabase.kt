package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 13,
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

        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN learningGoal TEXT NOT NULL DEFAULT 'Giao tiếp'")
                db.execSQL("UPDATE users SET englishLevel = 'B1' WHERE englishLevel = 'Intermediate' OR englishLevel = 'B1 - Intermediate'")
                db.execSQL("UPDATE users SET englishLevel = 'A2' WHERE englishLevel = 'Beginner' OR englishLevel = 'A1 - Beginner'")
                db.execSQL("UPDATE users SET englishLevel = 'C1' WHERE englishLevel = 'Advanced' OR englishLevel = 'C1 - Advanced'")
            }
        }

        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Ensure all level strings match Enum names (A1, A2, B1, B2, C1, C2)
                // This is mostly handled by MIGRATION_11_12 but we do it for vocabulary_sets too
                db.execSQL("UPDATE vocabulary_sets SET level = 'A1' WHERE level NOT IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')")
                db.execSQL("UPDATE users SET englishLevel = 'A1' WHERE englishLevel NOT IN ('A1', 'A2', 'B1', 'B2', 'C1', 'C2')")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minlish_database"
                )
                .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                .addMigrations(MIGRATION_11_12, MIGRATION_12_13)
                .fallbackToDestructiveMigrationOnDowngrade()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
