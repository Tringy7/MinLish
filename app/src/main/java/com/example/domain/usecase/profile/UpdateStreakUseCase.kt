package com.example.domain.usecase.profile

import com.example.data.local.entity.UserEntity
import com.example.domain.repository.UserRepository
import java.util.Calendar

class UpdateStreakUseCase(private val repository: UserRepository) {
    suspend operator fun invoke() {
        val user = repository.getUser() ?: UserEntity(
            name = "Learner",
            email = "learner@minlish.com",
            passwordHash = ""
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
            lastStudy == todayStart -> user.streakCount
            todayStart - lastStudy <= oneDayMs -> user.streakCount + 1
            else -> 1
        }

        val updatedUser = user.copy(
            streakCount = nextStreak,
            lastStudyDate = todayStart
        )
        repository.saveUser(updatedUser)
    }
}
