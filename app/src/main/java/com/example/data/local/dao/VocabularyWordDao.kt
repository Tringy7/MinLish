package com.example.data.local.dao
import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyWordDao {
    // --- Vocabulary Word Queries ---
    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId AND userId = :userId ORDER BY word ASC")
    fun getWordsBySetFlow(setId: Int, userId: Int): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId AND userId = :userId ORDER BY word ASC")
    suspend fun getWordsBySet(setId: Int, userId: Int): List<VocabularyWordEntity>

    @Query("SELECT * FROM vocabulary_words WHERE id = :wordId LIMIT 1")
    suspend fun getWordById(wordId: Int): VocabularyWordEntity?

    @Query("SELECT * FROM vocabulary_words WHERE isFavorite = 1 AND userId = :userId")
    fun getFavoriteWordsFlow(userId: Int): Flow<List<VocabularyWordEntity>>

    // Words due for review (nextReviewTimestamp <= currentTimestamp) in a specific set
    @Query("SELECT * FROM vocabulary_words WHERE setId = :setId AND userId = :userId AND nextReviewTimestamp <= :currentTimestamp")
    fun getDueWordsForSetFlow(setId: Int, userId: Int, currentTimestamp: Long): Flow<List<VocabularyWordEntity>>

    // All words due for review overall
    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND nextReviewTimestamp <= :currentTimestamp")
    fun getAllDueWordsFlow(userId: Int, currentTimestamp: Long): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND nextReviewTimestamp <= :currentTimestamp")
    suspend fun getAllDueWords(userId: Int, currentTimestamp: Long): List<VocabularyWordEntity>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND nextReviewTimestamp <= :currentTimestamp")
    fun getDueWordsCountFlow(userId: Int, currentTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId")
    fun getTotalWordsCountFlow(userId: Int): Flow<Int>

    // Count of words that have been studied at least once (lastReviewedTimestamp > 0)
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND lastReviewedTimestamp > 0")
    fun getLearnedWordsCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND repetitions > 5")
    fun getMasteredWordsCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND repetitions > 0 AND repetitions <= 5")
    fun getLearningWordsCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND lastQuality = 0")
    fun getAgainCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND lastQuality = 3")
    fun getHardCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND lastQuality = 4")
    fun getGoodCountFlow(userId: Int): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE userId = :userId AND lastQuality = 5")
    fun getEasyCountFlow(userId: Int): Flow<Int>

    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND lastQuality = 0")
    fun getAgainWordsFlow(userId: Int): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND lastQuality = 3")
    fun getHardWordsFlow(userId: Int): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND lastQuality = 4")
    fun getGoodWordsFlow(userId: Int): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE userId = :userId AND lastQuality = 5")
    fun getEasyWordsFlow(userId: Int): Flow<List<VocabularyWordEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWord(word: VocabularyWordEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWords(words: List<VocabularyWordEntity>)
    
    @Upsert
    suspend fun upsertWord(word: VocabularyWordEntity)

    @Update
    suspend fun updateWord(word: VocabularyWordEntity)

    @Delete
    suspend fun deleteWord(word: VocabularyWordEntity)
}
