package com.example.domain.model

data class DashboardStats(
    val totalWordsCount: Int = 0,
    val learnedWordsCount: Int = 0,
    val currentStreak: Int = 0,
    val retentionRate: Int = 100,
    val dueTodayCount: Int = 0,
    val newWordsTodayCount: Int = 0,
    val dailyActivities: List<DailyActivity> = emptyList()
)

data class DailyActivity(val dayLabel: String, val count: Int)