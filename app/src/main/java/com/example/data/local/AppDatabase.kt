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
    version = 2,
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

        // Script Migration từ v1 lên v2 để không mất dữ liệu cũ
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Cập nhật bảng users (loại bỏ cột 'role')
                database.execSQL("CREATE TABLE IF NOT EXISTS `users_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `email` TEXT NOT NULL, `name` TEXT NOT NULL, `passwordHash` TEXT, `provider` TEXT NOT NULL, `avatarUrl` TEXT NOT NULL, `englishLevel` TEXT NOT NULL, `learningGoal` TEXT NOT NULL, `streakCount` INTEGER NOT NULL, `lastStudyDate` INTEGER NOT NULL, `totalXp` INTEGER NOT NULL, `dailyGoalWords` INTEGER NOT NULL, `lastLoginAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
                database.execSQL("INSERT INTO `users_new` (id, email, name, passwordHash, provider, avatarUrl, englishLevel, learningGoal, streakCount, lastStudyDate, totalXp, dailyGoalWords, lastLoginAt, createdAt) SELECT id, email, name, passwordHash, provider, avatarUrl, englishLevel, learningGoal, streakCount, lastStudyDate, totalXp, dailyGoalWords, lastLoginAt, createdAt FROM `users`")
                database.execSQL("DROP TABLE `users`")
                database.execSQL("ALTER TABLE `users_new` RENAME TO `users`")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_users_email` ON `users` (`email`)")

                // 2. Cập nhật bảng vocabulary_words (loại bỏ cột 'userId')
                database.execSQL("CREATE TABLE IF NOT EXISTS `vocabulary_words_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `setId` INTEGER NOT NULL, `word` TEXT NOT NULL, `pronunciation` TEXT NOT NULL, `meaning` TEXT NOT NULL, `example` TEXT NOT NULL, `note` TEXT NOT NULL, `descriptionEN` TEXT NOT NULL, `collocations` TEXT NOT NULL, `relatedWords` TEXT NOT NULL, `isFavorite` INTEGER NOT NULL, `repetitions` INTEGER NOT NULL, `intervalDays` INTEGER NOT NULL, `easeFactor` REAL NOT NULL, `nextReviewTimestamp` INTEGER NOT NULL, `lastReviewedTimestamp` INTEGER NOT NULL, `lastQuality` INTEGER NOT NULL, FOREIGN KEY(`setId`) REFERENCES `vocabulary_sets`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
                database.execSQL("INSERT INTO `vocabulary_words_new` (id, setId, word, pronunciation, meaning, example, note, descriptionEN, collocations, relatedWords, isFavorite, repetitions, intervalDays, easeFactor, nextReviewTimestamp, lastReviewedTimestamp, lastQuality) SELECT id, setId, word, pronunciation, meaning, example, note, descriptionEN, collocations, relatedWords, isFavorite, repetitions, intervalDays, easeFactor, nextReviewTimestamp, lastReviewedTimestamp, lastQuality FROM `vocabulary_words`")
                database.execSQL("DROP TABLE `vocabulary_words`")
                database.execSQL("ALTER TABLE `vocabulary_words_new` RENAME TO `vocabulary_words`")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_setId` ON `vocabulary_words` (`setId`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_nextReviewTimestamp` ON `vocabulary_words` (`nextReviewTimestamp`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_vocabulary_words_isFavorite` ON `vocabulary_words` (`isFavorite`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "minlish.db"
                )
                    .addMigrations(MIGRATION_1_2) // Thêm migration tại đây
                    .fallbackToDestructiveMigration(true) // Chỉ xóa nếu không tìm thấy script migration
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}