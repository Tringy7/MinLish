package com.example.domain.usecase.vocabulary

import com.example.domain.repository.VocabularyWordRepository
import com.example.utils.CsvHelper

class ImportWordsUseCase(private val repository: VocabularyWordRepository) {
    suspend operator fun invoke(setId: Int, csvData: String, userId: Int) {
        val words = CsvHelper.parseCsv(csvData, setId, userId)
        if (words.isNotEmpty()) {
            repository.insertWords(words)
        }
    }
}
