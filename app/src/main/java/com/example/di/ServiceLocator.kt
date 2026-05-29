package com.example.di

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.repository.VocabularyRepositoryImpl
import com.example.domain.repository.VocabularyRepository

object ServiceLocator {
    
    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var repository: VocabularyRepository? = null

    fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val db = database ?: AppDatabase.getDatabase(context)
            database = db
            db
        }
    }

    fun getRepository(context: Context): VocabularyRepository {
        return repository ?: synchronized(this) {
            val repo = repository ?: VocabularyRepositoryImpl(
                getDatabase(context).vocabularyDao()
            )
            repository = repo
            repo
        }
    }
}
