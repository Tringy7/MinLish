package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistory(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history WHERE userId = :userId ORDER BY reviewedAt DESC LIMIT :limit")
    fun getRecentHistoryFlow(userId: Int, limit: Int): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT * FROM review_history WHERE userId = :userId ORDER BY reviewedAt ASC")
    fun getAllHistoryFlow(userId: Int): Flow<List<ReviewHistoryEntity>>
  
    @Query("SELECT COUNT(*) FROM review_history WHERE userId = :userId AND reviewedAt >= :startTime")
    fun getReviewCountSinceFlow(userId: Int, startTime: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT wordId) FROM review_history WHERE userId = :userId AND reviewedAt >= :startTime")
    fun getUniqueWordsReviewedSinceFlow(userId: Int, startTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_history WHERE userId = :userId")
    fun getTotalReviewsFlow(userId: Int): Flow<Int>

    @Query("SELECT * FROM review_history WHERE userId = :userId")
    suspend fun getAllHistories(userId: Int): List<ReviewHistoryEntity>
}
