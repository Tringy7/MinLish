package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.repository.*
import com.example.domain.repository.*

object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var sessionManager: SessionManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getDatabase(context).also { database = it }
        }
    }

    fun getSessionManager(context: Context): SessionManager {
        return sessionManager ?: synchronized(this) {
            val manager = sessionManager ?: SessionManager(context)
            sessionManager = manager
            manager
        }
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            val repo = authRepository ?: AuthRepositoryImpl(
                getDatabase(context).userDao(),
                getSessionManager(context)
            )
            authRepository = repo
            repo
        }
    }

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(
            getDatabase(context).userDao(),
            getSessionManager(context)
        )
    }

    fun provideVocabularySetRepository(context: Context): VocabularySetRepository {
        return VocabularySetRepositoryImpl(
            getDatabase(context).vocabularySetDao(),
            getSessionManager(context)
        )
    }

    fun provideVocabularyWordRepository(context: Context): VocabularyWordRepository {
        return VocabularyWordRepositoryImpl(
            getDatabase(context).vocabularyWordDao(),
            getSessionManager(context)
        )
    }

    fun provideReviewHistoryRepository(context: Context): ReviewHistoryRepository {
        return ReviewHistoryRepositoryImpl(
            getDatabase(context).reviewHistoryDao(),
            getSessionManager(context)
        )
    }
}
