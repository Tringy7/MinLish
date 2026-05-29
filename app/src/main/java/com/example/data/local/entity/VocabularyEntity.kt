package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "local_user",
    val name: String,
    val email: String,
    val avatarUrl: String = "",
    val englishLevel: String = "Intermediate",
    val streakCount: Int = 0,
    val lastStudyDate: Long = 0L
)

@Entity(tableName = "vocabulary_sets")
data class VocabularySetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val tags: String, // Comma separated tags
    val userId: String = "local_user",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "vocabulary_words",
    foreignKeys = [
        ForeignKey(
            entity = VocabularySetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["setId"])]
)
data class VocabularyWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: Int,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val example: String,
    val note: String,
    val isFavorite: Boolean = false,
    
    // Spaced Repetition State (SM-2 parameters)
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val lastReviewedTimestamp: Long = 0L
)

@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(
            entity = VocabularyWordEntity::class,
            parentColumns = ["id"],
            childColumns = ["wordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["wordId"])]
)
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: Int,
    val rating: Int, // 1: Again, 2: Hard, 3: Good, 4: Easy
    val reviewedAt: Long = System.currentTimeMillis()
)
