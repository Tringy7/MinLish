package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.remote.AuthAuthenticator
import com.example.data.remote.AuthInterceptor
import com.example.data.repository.*
import com.example.data.security.JwtService
import com.example.data.security.TokenManager
import com.example.domain.repository.*
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var tokenManager: TokenManager? = null

    @Volatile
    private var jwtService: JwtService? = null

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

    fun getTokenManager(context: Context): TokenManager {
        return tokenManager ?: synchronized(this) {
            val manager = tokenManager ?: TokenManager(context)
            tokenManager = manager
            manager
        }
    }

    fun getJwtService(): JwtService {
        return jwtService ?: synchronized(this) {
            val service = jwtService ?: JwtService()
            jwtService = service
            service
        }
    }

    private fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            okHttpClient ?: OkHttpClient.Builder()
                .addInterceptor(AuthInterceptor(getTokenManager(context)))
                .authenticator(AuthAuthenticator(getTokenManager(context), getJwtService(), getDatabase(context).userDao()))
                .build()
                .also { okHttpClient = it }
        }
    }

    fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl("https://your-api-endpoint.com/") 
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
                getTokenManager(context),
                getJwtService()
            )
            authRepository = repo
            repo
        }
    }

    fun provideUserRepository(context: Context): UserRepository {
        return UserRepositoryImpl(
            getDatabase(context).userDao(),
            getTokenManager(context)
        )
    }

    fun provideVocabularySetRepository(context: Context): VocabularySetRepository {
        return VocabularySetRepositoryImpl(
            getDatabase(context).vocabularySetDao(),
            getTokenManager(context)
        )
    }

    fun provideVocabularyWordRepository(context: Context): VocabularyWordRepository {
        return VocabularyWordRepositoryImpl(
            getDatabase(context).vocabularyWordDao()
        )
    }

    fun provideReviewHistoryRepository(context: Context): ReviewHistoryRepository {
        return ReviewHistoryRepositoryImpl(
            getDatabase(context).reviewHistoryDao(),
            getTokenManager(context)
        )
    }
}
