package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    indices = [
        Index(value = ["setId"]),
        Index(value = ["nextReviewTimestamp"]),
        Index(value = ["isFavorite"])
    ]
)
data class VocabularyWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val setId: Int,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val example: String,
    val note: String,
    val descriptionEN: String = "",
    val collocations: String = "",
    val relatedWords: String = "",
    val isFavorite: Boolean = false,
    
    // Spaced Repetition State (SM-2 parameters)
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val easeFactor: Double = 2.5,
    val nextReviewTimestamp: Long = System.currentTimeMillis(),
    val lastReviewedTimestamp: Long = 0L,
    val lastQuality: Int = -1 // 0=Quên, 1=Lờ mờ, 2=Nhớ kịp, 3=Nhớ ngay
)
