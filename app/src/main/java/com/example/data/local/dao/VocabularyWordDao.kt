package com.example.data.local.dao
import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VocabularyWordDao {
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

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE nextReviewTimestamp <= :currentTimestamp")
    fun getDueWordsCountFlow(currentTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words")
    fun getTotalWordsCountFlow(): Flow<Int>

    // Count of words that have been studied at least once (lastReviewedTimestamp > 0)
    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE lastReviewedTimestamp > 0")
    fun getLearnedWordsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE repetitions > 5")
    fun getMasteredWordsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE repetitions > 0 AND repetitions <= 5")
    fun getLearningWordsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE lastQuality = 0")
    fun getAgainCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE lastQuality = 3")
    fun getHardCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE lastQuality = 4")
    fun getGoodCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM vocabulary_words WHERE lastQuality = 5")
    fun getEasyCountFlow(): Flow<Int>

    @Query("SELECT * FROM vocabulary_words WHERE lastQuality = 0")
    fun getAgainWordsFlow(): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE lastQuality = 3")
    fun getHardWordsFlow(): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE lastQuality = 4")
    fun getGoodWordsFlow(): Flow<List<VocabularyWordEntity>>

    @Query("SELECT * FROM vocabulary_words WHERE lastQuality = 5")
    fun getEasyWordsFlow(): Flow<List<VocabularyWordEntity>>

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
