package com.example.data.repository

import com.example.data.local.SessionManager
import com.example.data.local.dao.VocabularyWordDao
import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyWordRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class VocabularyWordRepositoryImpl(
    private val vocabularyWordDao: VocabularyWordDao,
    private val sessionManager: SessionManager
) : VocabularyWordRepository {

    override fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getWordsBySetFlow(setId, userId)
        }

    override suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity> {
        val userId = sessionManager.currentUserId.first() ?: return emptyList()
        return vocabularyWordDao.getWordsBySet(setId, userId)
    }

    override suspend fun getWordById(id: Int): VocabularyWordEntity? = vocabularyWordDao.getWordById(id)

    override fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getFavoriteWordsFlow(userId)
        }

    override fun getDueWordsForSetFlow(setId: Int, timestamp: Long): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getDueWordsForSetFlow(setId, userId, timestamp)
        }

    override fun getAllDueWordsFlow(timestamp: Long): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getAllDueWordsFlow(userId, timestamp)
        }

    override suspend fun getAllDueWords(timestamp: Long): List<VocabularyWordEntity> {
        val userId = sessionManager.currentUserId.first() ?: return emptyList()
        return vocabularyWordDao.getAllDueWords(userId, timestamp)
    }

    override fun getDueWordsCountFlow(timestamp: Long): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getDueWordsCountFlow(userId, timestamp)
        }

    override fun getTotalWordsCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getTotalWordsCountFlow(userId)
        }

    override fun getLearnedWordsCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getLearnedWordsCountFlow(userId)
        }

    override fun getMasteredWordsCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getMasteredWordsCountFlow(userId)
        }

    override fun getLearningWordsCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getLearningWordsCountFlow(userId)
        }

    override fun getQuenCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getAgainCountFlow(userId)
        }

    override fun getLoMoCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getHardCountFlow(userId)
        }

    override fun getNhoKipCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getGoodCountFlow(userId)
        }

    override fun getNhoNgayCountFlow(): Flow<Int> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else vocabularyWordDao.getEasyCountFlow(userId)
        }

    override fun getQuenWordsFlow(): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getAgainWordsFlow(userId)
        }

    override fun getLoMoWordsFlow(): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getHardWordsFlow(userId)
        }

    override fun getNhoKipWordsFlow(): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getGoodWordsFlow(userId)
        }

    override fun getNhoNgayWordsFlow(): Flow<List<VocabularyWordEntity>> = 
        sessionManager.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else vocabularyWordDao.getEasyWordsFlow(userId)
        }

    override suspend fun insertWord(word: VocabularyWordEntity): Int {
        val userId = sessionManager.currentUserId.first() ?: return -1
        return vocabularyWordDao.insertWord(word.copy(userId = userId)).toInt()
    }

    override suspend fun insertWords(words: List<VocabularyWordEntity>) {
        val userId = sessionManager.currentUserId.first() ?: return
        val wordsWithUserId = words.map { it.copy(userId = userId) }
        vocabularyWordDao.insertWords(wordsWithUserId)
    }

    override suspend fun updateWord(word: VocabularyWordEntity) = vocabularyWordDao.updateWord(word)

    override suspend fun deleteWord(word: VocabularyWordEntity) = vocabularyWordDao.deleteWord(word)
}
