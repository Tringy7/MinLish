package com.example.domain.usecase.vocabulary

import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.repository.VocabularyWordRepository

class ManageVocabularyWordUseCase(private val repository: VocabularyWordRepository) {
    suspend fun addWord(
        setId: Int,
        wordTxt: String,
        ipa: String,
        meaningTxt: String,
        exampleTxt: String,
        noteTxt: String
    ): Int {
        if (wordTxt.isBlank() || meaningTxt.isBlank()) return -1
        return repository.insertWord(
            VocabularyWordEntity(
                setId = setId,
                word = wordTxt,
                pronunciation = ipa,
                meaning = meaningTxt,
                example = exampleTxt,
                note = noteTxt
            )
        )
    }

    suspend fun updateWord(word: VocabularyWordEntity) {
        repository.updateWord(word)
    }

    suspend fun deleteWord(word: VocabularyWordEntity) {
        repository.deleteWord(word)
    }

    suspend fun toggleFavorite(word: VocabularyWordEntity) {
        repository.updateWord(word.copy(isFavorite = !word.isFavorite))
    }

    fun getFavoriteWordsFlow() = repository.getFavoriteWordsFlow()
}
