package com.example.data.repository

import com.example.data.local.dao.ReviewHistoryDao
import com.example.data.local.entity.ReviewHistoryEntity
import com.example.domain.repository.ReviewHistoryRepository
import kotlinx.coroutines.flow.Flow

class ReviewHistoryRepositoryImpl(
    private val reviewHistoryDao: ReviewHistoryDao
) : ReviewHistoryRepository {
    override suspend fun insertHistory(history: ReviewHistoryEntity) = reviewHistoryDao.insertHistory(history).let { Unit }
    override fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>> = reviewHistoryDao.getRecentHistoryFlow(limit)
    override fun getAllHistoryFlow(): Flow<List<ReviewHistoryEntity>> = reviewHistoryDao.getAllHistoryFlow()
    override fun getTotalReviewsFlow(): Flow<Int> = reviewHistoryDao.getTotalReviewsFlow()
    override suspend fun getAllHistories(): List<ReviewHistoryEntity> = reviewHistoryDao.getAllHistories()
}
