package com.example.domain.repository

import com.example.data.local.entity.VocabularyWordEntity
import kotlinx.coroutines.flow.Flow

interface VocabularyWordRepository {
    fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>>
    suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity>
    suspend fun getWordById(id: Int): VocabularyWordEntity?
    fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>>
    fun getDueWordsForSetFlow(setId: Int, timestamp: Long): Flow<List<VocabularyWordEntity>>
    fun getAllDueWordsFlow(timestamp: Long): Flow<List<VocabularyWordEntity>>
    suspend fun getAllDueWords(timestamp: Long): List<VocabularyWordEntity>
    fun getDueWordsCountFlow(timestamp: Long): Flow<Int>
    fun getTotalWordsCountFlow(): Flow<Int>
    fun getLearnedWordsCountFlow(): Flow<Int>
    suspend fun insertWord(word: VocabularyWordEntity): Int
    suspend fun insertWords(words: List<VocabularyWordEntity>)
    suspend fun updateWord(word: VocabularyWordEntity)
    suspend fun deleteWord(word: VocabularyWordEntity)
}
