package com.example.data.repository

import com.example.data.local.dao.VocabularySetDao
import com.example.data.local.entity.VocabularySetEntity
import com.example.domain.repository.VocabularySetRepository
import kotlinx.coroutines.flow.Flow

class VocabularySetRepositoryImpl(
    private val vocabularySetDao: VocabularySetDao
) : VocabularySetRepository {
    override fun getAllSetsFlow(): Flow<List<VocabularySetEntity>> = vocabularySetDao.getAllSetsFlow()
    override suspend fun getSetById(id: Int): VocabularySetEntity? = vocabularySetDao.getSetById(id)
    override fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>> = vocabularySetDao.searchSetsFlow(query)
    override suspend fun insertSet(set: VocabularySetEntity): Int = vocabularySetDao.insertSet(set).toInt()
    override suspend fun updateSet(set: VocabularySetEntity) = vocabularySetDao.updateSet(set)
    override suspend fun deleteSet(set: VocabularySetEntity) = vocabularySetDao.deleteSet(set)
}
