package com.example.data.local.entity

import androidx.room.Entity
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