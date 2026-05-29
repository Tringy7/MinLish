package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyDao {

    // --- User Queries ---
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserFlow(userId: String = "local_user"): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUser(userId: String = "local_user"): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)


    // --- Vocabulary Set Queries ---
    @Query("SELECT * FROM vocabulary_sets ORDER BY createdAt DESC")
    fun getAllSetsFlow(): Flow<List<VocabularySetEntity>>

    @Query("SELECT * FROM vocabulary_sets WHERE id = :setId LIMIT 1")
    suspend fun getSetById(setId: Int): VocabularySetEntity?

    @Query("SELECT * FROM vocabulary_sets WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: VocabularySetEntity): Long

    @Update
    suspend fun updateSet(set: VocabularySetEntity)

    @Delete
    suspend fun deleteSet(set: VocabularySetEntity)


    // --- Vocabulary Word Queries ---
    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId ORDER BY word ASC")
    fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId ORDER BY word ASC")
    suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity>

    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Int): VocabularyWordEntity?

    @Query("SELECT * FROM vocabulary_words WHERE isFavorite = 1")
    fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>>

    // Words due for review (nextReviewTimestamp <= currentTimestamp) in a specific set
    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId AND nextReviewTimestamp <= :currentTimestamp")
    fun getDueWordsForSetFlow(setId: Int, currentTimestamp: Long): Flow<List<VocabularyWordEntity>>

    // All words due for review overall
    @Query("SELECT * FROM vocabulary_words WHERE nextReviewTimestamp <= :currentTimestamp")
    fun getAllDueWordsFlow(currentTimestamp: Long): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE nextReviewTimestamp <= :currentTimestamp")
    suspend fun getAllDueWords(currentTimestamp: Long): List<VocabularyWordEntity>

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    fun getTotalWordsCountFlow(): Flow<Int>

    // Count of words that have been studied at least once (repetitions > 0)
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE repetitions > 0")
    fun getLearnedWordsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: VocabularyWordEntity): Long

    @Update
    suspend fun updateWord(word: VocabularyWordEntity)

    @Delete
    suspend fun deleteWord(word: VocabularyWordEntity)


    // --- Review History Queries ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: ReviewHistoryEntity): Long

    @Query("SELECT * FROM review_history ORDER BY reviewedAt DESC LIMIT :limit")
    fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>>

    @Query("SELECT COUNT(*) FROM review_history")
    fun getTotalReviewsFlow(): Flow<Int>

    @Query("SELECT * FROM review_history")
    suspend fun getAllHistories(): List<ReviewHistoryEntity>
}
