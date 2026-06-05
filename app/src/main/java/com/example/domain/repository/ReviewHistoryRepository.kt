package com.example.domain.repository

import com.example.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

interface ReviewHistoryRepository {
    suspend fun insertHistory(history: ReviewHistoryEntity)
    fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>>
    fun getReviewCountSinceFlow(startTime: Long): Flow<Int>
    fun getUniqueWordsReviewedSinceFlow(startTime: Long): Flow<Int>
    fun getTotalReviewsFlow(): Flow<Int>
    suspend fun getAllHistories(): List<ReviewHistoryEntity>
}
