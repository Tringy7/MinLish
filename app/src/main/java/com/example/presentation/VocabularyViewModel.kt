package com.example.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.data.local.entity.*
import com.example.domain.repository.VocabularyRepository
import com.example.domain.usecase.auth.*
import com.example.domain.usecase.home.*
import com.example.domain.usecase.vocabulary.*
import com.example.domain.usecase.flashcard.*
import com.example.domain.usecase.profile.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Typealiases to maintain complete compatibility with old screen imports 
// while cleanly architecting models inside domain library
typealias DashboardStats = com.example.domain.model.DashboardStats
typealias DailyActivity = com.example.domain.model.DailyActivity

class VocabularyViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getVocabularySetsUseCase: GetVocabularySetsUseCase,
    private val getWordsInSetUseCase: GetWordsInSetUseCase,
    private val manageVocabularySetUseCase: ManageVocabularySetUseCase,
    private val manageVocabularyWordUseCase: ManageVocabularyWordUseCase,
    private val getDueWordsUseCase: GetDueWordsUseCase,
    private val reviewWordUseCase: ReviewWordUseCase,
    private val updateEnglishLevelUseCase: UpdateEnglishLevelUseCase
) : ViewModel() {

    // --- Authentication & User States ---
    private val _isUserLoggedIn = MutableStateFlow(true) // Simulating session start active state
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    // Retrieve user identity using GetUserUseCase
    val userState: StateFlow<UserEntity?> = getUserUseCase.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Vocabulary Sets ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // FlatMap latest searches using GetVocabularySetsUseCase
    val wordSets: StateFlow<List<VocabularySetEntity>> = _searchQuery
        .flatMapLatest { query ->
            getVocabularySetsUseCase(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Set and Words ---
    private val _currentSetId = MutableStateFlow<Int?>(null)
    val currentSetId = _currentSetId.asStateFlow()

    val currentSet: StateFlow<VocabularySetEntity?> = _currentSetId
        .map { id -> id?.let { getVocabularySetsUseCase.getById(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wordsInCurrentSet: StateFlow<List<VocabularyWordEntity>> = _currentSetId
        .flatMapLatest { id ->
            getWordsInSetUseCase(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Favorite Words ---
    val favoriteWords: StateFlow<List<VocabularyWordEntity>> = manageVocabularyWordUseCase.getFavoriteWordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Spaced Repetition Due Lists ---
    val allDueWords: StateFlow<List<VocabularyWordEntity>> = getDueWordsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard & Analytical Statistics ---
    val dashboardStats: StateFlow<DashboardStats> = getDashboardStatsUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())


    // --- Actions & Methods ---

    fun login(email: String, name: String) {
        viewModelScope.launch {
            loginUseCase(email, name)
            _isUserLoggedIn.value = true
        }
    }

    fun logout() {
        _isUserLoggedIn.value = logoutUseCase()
    }

    fun selectSet(setId: Int?) {
        _currentSetId.value = setId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Word CRUD Management using Dedicated UseCases ---

    fun addWord(setId: Int, wordTxt: String, ipa: String, meaningTxt: String, exampleTxt: String, noteTxt: String) {
        viewModelScope.launch {
            manageVocabularyWordUseCase.addWord(setId, wordTxt, ipa, meaningTxt, exampleTxt, noteTxt)
        }
    }

    fun editWord(word: VocabularyWordEntity) {
        viewModelScope.launch {
            manageVocabularyWordUseCase.updateWord(word)
        }
    }

    fun deleteWord(word: VocabularyWordEntity) {
        viewModelScope.launch {
            manageVocabularyWordUseCase.deleteWord(word)
        }
    }

    fun toggleFavorite(word: VocabularyWordEntity) {
        viewModelScope.launch {
            manageVocabularyWordUseCase.toggleFavorite(word)
        }
    }

    // --- Vocabulary Set CRUD Management ---

    fun addSet(name: String, description: String, tags: String) {
        viewModelScope.launch {
            manageVocabularySetUseCase.addSet(name, description, tags)
        }
    }

    fun editSet(set: VocabularySetEntity) {
        viewModelScope.launch {
            manageVocabularySetUseCase.updateSet(set)
        }
    }

    fun deleteSet(set: VocabularySetEntity) {
        viewModelScope.launch {
            manageVocabularySetUseCase.deleteSet(set)
        }
    }

    fun updateEnglishLevel(level: String) {
        viewModelScope.launch {
            updateEnglishLevelUseCase(level)
        }
    }

    // --- Spaced Repetition Study Engine ---

    fun reviewWordResponse(word: VocabularyWordEntity, rating: Int) {
        viewModelScope.launch {
            reviewWordUseCase(word, rating)
        }
    }

    // --- ViewModel Factory ---
    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
                val repository = ServiceLocator.getRepository(context)
                
                // Construct and inject proper Use Cases
                return VocabularyViewModel(
                    getUserUseCase = GetUserUseCase(repository),
                    loginUseCase = LoginUseCase(repository),
                    logoutUseCase = LogoutUseCase(),
                    getDashboardStatsUseCase = GetDashboardStatsUseCase(repository),
                    getVocabularySetsUseCase = GetVocabularySetsUseCase(repository),
                    getWordsInSetUseCase = GetWordsInSetUseCase(repository),
                    manageVocabularySetUseCase = ManageVocabularySetUseCase(repository),
                    manageVocabularyWordUseCase = ManageVocabularyWordUseCase(repository),
                    getDueWordsUseCase = GetDueWordsUseCase(repository),
                    reviewWordUseCase = ReviewWordUseCase(repository),
                    updateEnglishLevelUseCase = UpdateEnglishLevelUseCase(repository)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
