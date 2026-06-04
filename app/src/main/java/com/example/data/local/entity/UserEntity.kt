package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String,
    val passwordHash: String,
    val avatarUrl: String = "",
    val englishLevel: String = "Intermediate",
    val streakCount: Int = 0,
    val lastStudyDate: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
