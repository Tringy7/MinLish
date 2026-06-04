package com.example.domain.repository

import com.example.data.local.entity.VocabularySetEntity
import kotlinx.coroutines.flow.Flow

interface VocabularySetRepository {
    fun getAllSetsFlow(): Flow<List<VocabularySetEntity>>
    suspend fun getSetById(id: Int): VocabularySetEntity?
    fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>>
    suspend fun insertSet(set: VocabularySetEntity): Int
    suspend fun updateSet(set: VocabularySetEntity)
    suspend fun deleteSet(set: VocabularySetEntity)
}
