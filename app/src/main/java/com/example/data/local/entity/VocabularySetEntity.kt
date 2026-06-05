package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vocabulary_sets")
data class VocabularySetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val tags: String, // Comma separated tags
    val level: String = "A1", // A1, A2, B1, B2, C1
    val category: String = "General", // Family, Food, Business...
    val isSystem: Boolean = false, // True for pre-packaged sets
    val userId: Int?, // Map to UserEntity.id, null for system sets
    val createdAt: Long = System.currentTimeMillis()
)
