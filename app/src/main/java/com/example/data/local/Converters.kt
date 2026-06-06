package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.EnglishLevel

class Converters {
    @TypeConverter
    fun fromEnglishLevel(level: EnglishLevel): String {
        return level.name
    }

    @TypeConverter
    fun toEnglishLevel(value: String): EnglishLevel {
        return try {
            EnglishLevel.valueOf(value)
        } catch (e: IllegalArgumentException) {
            EnglishLevel.A1
        }
    }
}
