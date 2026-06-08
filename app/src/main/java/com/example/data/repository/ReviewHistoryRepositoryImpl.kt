package com.example.data.repository

import com.example.data.local.dao.ReviewHistoryDao
import com.example.data.local.entity.ReviewHistoryEntity
import com.example.data.security.TokenManager
import com.example.domain.repository.ReviewHistoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewHistoryRepositoryImpl(
    private val reviewHistoryDao: ReviewHistoryDao,
    private val tokenManager: TokenManager
) : ReviewHistoryRepository {
    override suspend fun insertHistory(history: ReviewHistoryEntity) = reviewHistoryDao.insertHistory(history).let { Unit }
    
    override fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>> = 
        tokenManager.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else reviewHistoryDao.getRecentHistoryFlow(userId, limit)
        }

    override fun getAllHistoryFlow(): Flow<List<ReviewHistoryEntity>> = 
        tokenManager.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else reviewHistoryDao.getAllHistoryFlow(userId)
        }

    override fun getReviewCountSinceFlow(startTime: Long): Flow<Int> = 
        tokenManager.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else reviewHistoryDao.getReviewCountSinceFlow(userId, startTime)
        }

    override fun getUniqueWordsReviewedSinceFlow(startTime: Long): Flow<Int> = 
        tokenManager.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else reviewHistoryDao.getUniqueWordsReviewedSinceFlow(userId, startTime)
        }

    override fun getTotalReviewsFlow(): Flow<Int> = 
        tokenManager.userId.flatMapLatest { userId ->
            if (userId == null) flowOf(0)
            else reviewHistoryDao.getTotalReviewsFlow(userId)
        }

    override suspend fun getAllHistories(): List<ReviewHistoryEntity> {
        val userId = tokenManager.userId.first()
        return if (userId == null) emptyList()
        else reviewHistoryDao.getAllHistories(userId)
    }
}