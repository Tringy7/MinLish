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
    fun getMasteredWordsCountFlow(): Flow<Int>
    fun getLearningWordsCountFlow(): Flow<Int>
    fun getQuenCountFlow(): Flow<Int>
    fun getLoMoCountFlow(): Flow<Int>
    fun getNhoKipCountFlow(): Flow<Int>
    fun getNhoNgayCountFlow(): Flow<Int>
    fun getQuenWordsFlow(): Flow<List<VocabularyWordEntity>>
    fun getLoMoWordsFlow(): Flow<List<VocabularyWordEntity>>
    fun getNhoKipWordsFlow(): Flow<List<VocabularyWordEntity>>
    fun getNhoNgayWordsFlow(): Flow<List<VocabularyWordEntity>>
    suspend fun insertWord(word: VocabularyWordEntity): Int
    suspend fun insertWords(words: List<VocabularyWordEntity>)
    suspend fun updateWord(word: VocabularyWordEntity)
    suspend fun deleteWord(word: VocabularyWordEntity)
}
