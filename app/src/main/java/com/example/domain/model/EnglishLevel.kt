package com.example.domain.model

enum class EnglishLevel(val label: String, val description: String) {
    A1("A1", "Người mới bắt đầu"),
    A2("A2", "Cơ bản"),
    B1("B1", "Trung cấp"),
    B2("B2", "Trung cấp cao"),
    C1("C1", "Nâng cao"),
    C2("C2", "Thành thạo");

    val fullLabel: String
        get() = "$label - $description"

    companion object {
        fun fromString(value: String?): EnglishLevel {
            return entries.find { it.label.equals(value, ignoreCase = true) } ?: A1
        }
    }
}
