package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_sets")
data class VocabularySetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val tags: String, // Comma separated tags
    val userId: String = "local_user",
    val createdAt: Long = System.currentTimeMillis()
)
