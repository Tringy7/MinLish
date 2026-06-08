package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AuthManager
import com.example.data.remote.AuthInterceptor
import com.example.data.repository.*
import com.example.domain.repository.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var authManager: AuthManager? = null

    @Volatile
    private var authRepository: AuthRepository? = null

    @Volatile
    private var okHttpClient: OkHttpClient? = null

    @Volatile
    private var retrofit: Retrofit? = null

    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getDatabase(context).also { database = it }
        }
    }

    fun getAuthManager(context: Context): AuthManager {
        return authManager ?: synchronized(this) {
            val manager = authManager ?: AuthManager(context)
            authManager = manager
            manager
        }
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(getAuthManager(context)))
                .build()
                .also { okHttpClient = it }
        }
    }

    fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl("https://your-api-endpoint.com/") // Replace with your real API URL
                .client(getOkHttpClient(context))
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .also { retrofit = it }
        }
    }

    fun provideAuthRepository(context: Context): AuthRepository {
        return authRepository ?: synchronized(this) {
            val repo = authRepository ?: AuthRepositoryImpl(
                getDatabase(context).userDao(),
                getAuthManager(context)
            )
            authRepository = repo
            repo
        }
    }

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(
            getDatabase(context).userDao(),
            getAuthManager(context)
        )
    }

    fun provideVocabularySetRepository(context: Context): VocabularySetRepository {
        return VocabularySetRepositoryImpl(
            getDatabase(context).vocabularySetDao(),
            getAuthManager(context)
        )
    }

    fun provideVocabularyWordRepository(context: Context): VocabularyWordRepository {
        return VocabularyWordRepositoryImpl(
            getDatabase(context).vocabularyWordDao(),
            getAuthManager(context)
        )
    }

    fun provideReviewHistoryRepository(context: Context): ReviewHistoryRepository {
        return ReviewHistoryRepositoryImpl(
            getDatabase(context).reviewHistoryDao(),
            getAuthManager(context)
        )
    }
}
