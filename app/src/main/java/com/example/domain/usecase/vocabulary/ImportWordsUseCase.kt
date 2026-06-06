package com.example.domain.usecase.vocabulary

import com.example.domain.repository.VocabularyWordRepository
import com.example.utils.CsvHelper

class ImportWordsUseCase(private val repository: VocabularyWordRepository) {
    suspend operator fun invoke(setId: Int, csvData: String) {
        val words = CsvHelper.parseCsv(csvData, setId)
        if (words.isNotEmpty()) {
            repository.insertWords(words)
        }
    }
}
