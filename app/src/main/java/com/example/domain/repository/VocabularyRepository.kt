package com.example.domain.repository

import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

interface VocabularyRepository {

    // --- User Profile ---
    fun getUserFlow(): Flow<UserEntity?>
    suspend fun getUser(): UserEntity?
    suspend fun saveUser(user: UserEntity)
    suspend fun updateStreak()

    // --- Vocabulary Set ---
    fun getAllSetsFlow(): Flow<List<VocabularySetEntity>>
    suspend fun getSetById(id: Int): VocabularySetEntity?
    fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>>
    suspend fun insertSet(set: VocabularySetEntity): Int
    suspend fun updateSet(set: VocabularySetEntity)
    suspend fun deleteSet(set: VocabularySetEntity)

    // --- Vocabulary Word ---
    fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>>
    suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity>
    suspend fun getWordById(id: Int): VocabularyWordEntity?
    fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>>
    fun getDueWordsForSetFlow(setId: Int, timestamp: Long): Flow<List<VocabularyWordEntity>>
    fun getAllDueWordsFlow(timestamp: Long): Flow<List<VocabularyWordEntity>>
    suspend fun getAllDueWords(timestamp: Long): List<VocabularyWordEntity>
    fun getTotalWordsCountFlow(): Flow<Int>
    fun getLearnedWordsCountFlow(): Flow<Int>
    suspend fun insertWord(word: VocabularyWordEntity): Int
    suspend fun updateWord(word: VocabularyWordEntity)
    suspend fun deleteWord(word: VocabularyWordEntity)

    // --- Review History & Spaced Repetition ---
    suspend fun reviewWord(word: VocabularyWordEntity, rating: Int) // rating: 1 (Again), 2 (Hard), 3 (Good), 4 (Easy)
    fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>>
    fun getTotalReviewsFlow(): Flow<Int>
    suspend fun getAllHistories(): List<ReviewHistoryEntity>
}
