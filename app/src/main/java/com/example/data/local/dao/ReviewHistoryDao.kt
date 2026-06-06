package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.ReviewHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewHistoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistory(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history ORDER BY reviewedAt DESC LIMIT :limit")
    fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT COUNT(*) FROM review_history WHERE reviewedAt >= :startTime")
    fun getReviewCountSinceFlow(startTime: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT wordId) FROM review_history WHERE reviewedAt >= :startTime")
    fun getUniqueWordsReviewedSinceFlow(startTime: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM review_history")
    fun getTotalReviewsFlow(): Flow<Int>

    @Query("SELECT * FROM review_history")
    suspend fun getAllHistories(): List<ReviewHistoryEntity>
}
