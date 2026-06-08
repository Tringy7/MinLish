package com.example.data.repository

import com.example.data.local.dao.VocabularyWordDao
import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyWordRepository
import kotlinx.coroutines.flow.Flow

class VocabularyWordRepositoryImpl(
    private val vocabularyWordDao: VocabularyWordDao
) : VocabularyWordRepository {
    override fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getWordsBySetFlow(setId)
    override suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity> = vocabularyWordDao.getWordsBySet(setId)
    override suspend fun getWordById(id: Int): VocabularyWordEntity? = vocabularyWordDao.getWordById(id)
    override fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getFavoriteWordsFlow()
    override fun getDueWordsForSetFlow(setId: Int, timestamp: Long): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getDueWordsForSetFlow(setId, timestamp)
    override fun getAllDueWordsFlow(timestamp: Long): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getAllDueWordsFlow(timestamp)
    override suspend fun getAllDueWords(timestamp: Long): List<VocabularyWordEntity> = vocabularyWordDao.getAllDueWords(timestamp)
    override fun getDueWordsCountFlow(timestamp: Long): Flow<Int> = vocabularyWordDao.getDueWordsCountFlow(timestamp)
    override fun getTotalWordsCountFlow(): Flow<Int> = vocabularyWordDao.getTotalWordsCountFlow()
    override fun getLearnedWordsCountFlow(): Flow<Int> = vocabularyWordDao.getLearnedWordsCountFlow()
    override fun getMasteredWordsCountFlow(): Flow<Int> = vocabularyWordDao.getMasteredWordsCountFlow()
    override fun getLearningWordsCountFlow(): Flow<Int> = vocabularyWordDao.getLearningWordsCountFlow()
    override fun getQuenCountFlow(): Flow<Int> = vocabularyWordDao.getQuenCountFlow()
    override fun getLoMoCountFlow(): Flow<Int> = vocabularyWordDao.getLoMoCountFlow()
    override fun getNhoKipCountFlow(): Flow<Int> = vocabularyWordDao.getNhoKipCountFlow()
    override fun getNhoNgayCountFlow(): Flow<Int> = vocabularyWordDao.getNhoNgayCountFlow()
    override fun getQuenWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getQuenWordsFlow()
    override fun getLoMoWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getLoMoWordsFlow()
    override fun getNhoKipWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getNhoKipWordsFlow()
    override fun getNhoNgayWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyWordDao.getNhoNgayWordsFlow()
    override suspend fun insertWord(word: VocabularyWordEntity): Int = vocabularyWordDao.insertWord(word).toInt()
    override suspend fun insertWords(words: List<VocabularyWordEntity>) = vocabularyWordDao.insertWords(words)
    override suspend fun updateWord(word: VocabularyWordEntity) = vocabularyWordDao.updateWord(word)
    override suspend fun deleteWord(word: VocabularyWordEntity) = vocabularyWordDao.deleteWord(word)
}
