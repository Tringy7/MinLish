package com.example.domain.repository

import com.example.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ReviewHistoryRepository {
    suspend fun insertHistory(history: ReviewHistoryEntity)
    fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>>
    fun getAllHistoryFlow(): Flow<List<ReviewHistoryEntity>>
    fun getTotalReviewsFlow(): Flow<Int>
    suspend fun getAllHistories(): List<ReviewHistoryEntity>
}
