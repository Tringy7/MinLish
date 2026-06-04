package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.*
import com.example.domain.repository.*

/**
 * Service Locator for manual dependency injection.
 * Provides singleton instances of repositories and database.
 */
object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null

    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val db = AppDatabase.getDatabase(context)
            database = db
            db
        }
    }

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(getDatabase(context).userDao())
    }

    fun provideVocabularySetRepository(context: Context): VocabularySetRepository {
        return VocabularySetRepositoryImpl(getDatabase(context).vocabularySetDao())
    }

    fun provideVocabularyWordRepository(context: Context): VocabularyWordRepository {
        return VocabularyWordRepositoryImpl(getDatabase(context).vocabularyWordDao())
    }

    fun provideReviewHistoryRepository(context: Context): ReviewHistoryRepository {
        return ReviewHistoryRepositoryImpl(getDatabase(context).reviewHistoryDao())
    }
}
