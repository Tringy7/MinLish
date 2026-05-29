package com.example.domain.usecase.flashcard

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyRepository

class ReviewWordUseCase(private val repository: VocabularyRepository) {
    suspend operator fun invoke(word: VocabularyWordEntity, rating: Int) {
        repository.reviewWord(word, rating)
    }
}
