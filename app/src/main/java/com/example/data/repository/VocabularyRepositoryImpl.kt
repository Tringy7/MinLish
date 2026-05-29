package com.example.data.repository

import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.*
import com.example.domain.repository.VocabularyRepository
import com.example.domain.usecase.SpacedRepetitionCalculator
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class VocabularyRepositoryImpl(
    private val vocabularyDao: VocabularyDao
) : VocabularyRepository {

    override fun getUserFlow(): Flow<UserEntity?> = vocabularyDao.getUserFlow()

    override suspend fun getUser(): UserEntity? = vocabularyDao.getUser()

    override suspend fun saveUser(user: UserEntity) {
        vocabularyDao.insertUser(user)
    }

    override suspend fun updateStreak() {
        val user = vocabularyDao.getUser() ?: UserEntity(
            name = "Learner",
            email = "learner@minlish.com"
        )

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val todayStart = calendar.timeInMillis

        val lastStudy = user.lastStudyDate
        val oneDayMs = 24 * 60 * 60 * 1000L

        val nextStreak = when {
            lastStudy == 0L -> 1
            lastStudy == todayStart -> user.streakCount // Counted today already
            todayStart - lastStudy <= oneDayMs -> user.streakCount + 1 // Consecutive day
            else -> 1 // Streak broken
        }

        val updatedUser = user.copy(
            streakCount = nextStreak,
            lastStudyDate = todayStart
        )
        vocabularyDao.insertUser(updatedUser)
    }

    override fun getAllSetsFlow(): Flow<List<VocabularySetEntity>> = vocabularyDao.getAllSetsFlow()

    override suspend fun getSetById(id: Int): VocabularySetEntity? = vocabularyDao.getSetById(id)

    override fun searchSetsFlow(query: String): Flow<List<VocabularySetEntity>> = vocabularyDao.searchSetsFlow(query)

    override suspend fun insertSet(set: VocabularySetEntity): Int {
        return vocabularyDao.insertSet(set).toInt()
    }

    override suspend fun updateSet(set: VocabularySetEntity) {
        vocabularyDao.updateSet(set)
    }

    override suspend fun deleteSet(set: VocabularySetEntity) {
        vocabularyDao.deleteSet(set)
    }

    override fun getWordsBySetFlow(setId: Int): Flow<List<VocabularyWordEntity>> = vocabularyDao.getWordsBySetFlow(setId)

    override suspend fun getWordsBySet(setId: Int): List<VocabularyWordEntity> = vocabularyDao.getWordsBySet(setId)

    override suspend fun getWordById(id: Int): VocabularyWordEntity? = vocabularyDao.getWordById(id)

    override fun getFavoriteWordsFlow(): Flow<List<VocabularyWordEntity>> = vocabularyDao.getFavoriteWordsFlow()

    override fun getDueWordsForSetFlow(setId: Int, timestamp: Long): Flow<List<VocabularyWordEntity>> {
        return vocabularyDao.getDueWordsForSetFlow(setId, timestamp)
    }

    override fun getAllDueWordsFlow(timestamp: Long): Flow<List<VocabularyWordEntity>> {
        return vocabularyDao.getAllDueWordsFlow(timestamp)
    }

    override suspend fun getAllDueWords(timestamp: Long): List<VocabularyWordEntity> {
        return vocabularyDao.getAllDueWords(timestamp)
    }

    override fun getTotalWordsCountFlow(): Flow<Int> = vocabularyDao.getTotalWordsCountFlow()

    override fun getLearnedWordsCountFlow(): Flow<Int> = vocabularyDao.getLearnedWordsCountFlow()

    override suspend fun insertWord(word: VocabularyWordEntity): Int {
        return vocabularyDao.insertWord(word).toInt()
    }

    override suspend fun updateWord(word: VocabularyWordEntity) {
        vocabularyDao.updateWord(word)
    }

    override suspend fun deleteWord(word: VocabularyWordEntity) {
        vocabularyDao.deleteWord(word)
    }

    /**
     * Conducts a single card review, calls the SM-2 spaced repetition calculator,
     * logs the study result to histories, and updates user study streaks.
     */
    override suspend fun reviewWord(word: VocabularyWordEntity, rating: Int) {
        val sm2Result = SpacedRepetitionCalculator.calculate(
            repetitions = word.repetitions,
            previousIntervalDays = word.intervalDays,
            previousEaseFactor = word.easeFactor,
            userRating = rating
        )

        val updatedWord = word.copy(
            repetitions = sm2Result.repetitions,
            intervalDays = sm2Result.intervalDays,
            easeFactor = sm2Result.easeFactor,
            nextReviewTimestamp = sm2Result.nextReviewTimestamp,
            lastReviewedTimestamp = System.currentTimeMillis()
        )

        // Save review database updates
        vocabularyDao.updateWord(updatedWord)
        
        // Log to history
        val historyEntry = ReviewHistoryEntity(
            wordId = word.id,
            rating = rating,
            reviewedAt = System.currentTimeMillis()
        )
        vocabularyDao.insertHistory(historyEntry)

        // Increment user streak count
        updateStreak()
    }

    override fun getRecentHistoryFlow(limit: Int): Flow<List<ReviewHistoryEntity>> = vocabularyDao.getRecentHistoryFlow(limit)

    override fun getTotalReviewsFlow(): Flow<Int> = vocabularyDao.getTotalReviewsFlow()

    override suspend fun getAllHistories(): List<ReviewHistoryEntity> = vocabularyDao.getAllHistories()
}
