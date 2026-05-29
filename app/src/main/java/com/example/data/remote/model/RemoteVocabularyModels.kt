package com.example.data.remote.model

data class RemoteVocabularySetDto(
    val id: Int,
    val name: String,
    val description: String,
    val tags: String
)

data class RemoteVocabularyWordDto(
    val id: Int,
    val setId: Int,
    val word: String,
    val pronunciation: String,
    val meaning: String,
    val example: String,
    val note: String
)
