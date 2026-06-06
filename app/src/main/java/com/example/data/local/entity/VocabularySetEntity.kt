package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.EnglishLevel

@Entity(tableName = "vocabulary_sets")
data class VocabularySetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val tags: String, // Comma separated tags
    val level: EnglishLevel = EnglishLevel.A1,
    val category: String = "General",
    val isSystem: Boolean = false,
    val userId: Int?,
    val createdAt: Long = System.currentTimeMillis()
)
