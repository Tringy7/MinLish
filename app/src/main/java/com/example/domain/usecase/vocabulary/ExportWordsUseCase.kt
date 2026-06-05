package com.example.domain.usecase.vocabulary

import com.example.domain.repository.VocabularyWordRepository
import com.example.utils.CsvHelper

class ExportWordsUseCase(private val repository: VocabularyWordRepository) {
    suspend operator fun invoke(setId: Int): String {
        val words = repository.getWordsBySet(setId)
        return CsvHelper.exportToCsv(words)
    }
}
