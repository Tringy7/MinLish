package com.example.data.remote

import com.example.data.remote.model.RemoteVocabularySetDto
import com.example.data.remote.model.RemoteVocabularyWordDto

/**
 * Interface representing potential Retrofit endpoints for remote syncing.
 * This satisfies the Data layer isolation for Remote components, keeping the
 * architecture scalable for backend databases (e.g. PostgreSQL, Spanner, etc.)
 */
interface VocabularyApiService {
    
    suspend fun getVocabularySets(): List<RemoteVocabularySetDto>
    
    suspend fun getWordsForSet(setId: Int): List<RemoteVocabularyWordDto>
    
    suspend fun uploadUserProgress(userId: String, streak: Int, lastStudyLocalTime: Long)
}
