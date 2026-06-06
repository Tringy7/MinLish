package com.example.data.repository

import com.example.data.local.SessionManager
import com.example.data.local.dao.VocabularySetDao
import com.example.data.local.entity.VocabularySetEntity
import com.example.domain.repository.VocabularySetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class VocabularySetRepositoryImpl(
    private val vocabularySetDao: VocabularySetDao,
    private val sessionManager: SessionManager
) : VocabularySetRepository {

    override fun getAllSetsFlow(): Flow<List<VocabularySetEntity>> {
        return sessionManager.currentUserId.flatMapLatest { id ->
            // If user is not logged in, we still show system sets (userId = -1 for query)
            val userId = if (id != null && id != -1) id else -1
            vocabularySetDao.getAllAvailableSetsFlow(userId)
        }
    }

    override suspend fun getSetById(id: Int): VocabularySetEntity? = vocabularySetDao.getSetById(id)

    override fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>> {
        return sessionManager.currentUserId.flatMapLatest { id ->
            val userId = if (id != null && id != -1) id else -1
            vocabularySetDao.searchSetsFlow(userId, query)
        }
    }

    override suspend fun insertSet(set: VocabularySetEntity): Int {
        val userId = sessionManager.currentUserId.first() ?: return -1
        return vocabularySetDao.insertSet(set.copy(userId = userId)).toInt()
    }

    override suspend fun updateSet(set: VocabularySetEntity) = vocabularySetDao.updateSet(set)

    override suspend fun deleteSet(set: VocabularySetEntity) = vocabularySetDao.deleteSet(set)
}
