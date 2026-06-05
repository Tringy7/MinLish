package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AuthProvider

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val name: String,
    val passwordHash: String? = null, // Null for Google Login
    val provider: AuthProvider = AuthProvider.LOCAL,
    val avatarUrl: String = "",
    val englishLevel: String = "Intermediate",
    val streakCount: Int = 0,
    val lastStudyDate: Long = 0L,
    val totalXp: Int = 0,
    val dailyGoalWords: Int = 20,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
