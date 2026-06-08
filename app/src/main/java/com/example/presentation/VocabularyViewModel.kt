package com.example.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.di.ServiceLocator
import com.example.data.local.entity.*
import com.example.domain.model.EnglishLevel
import com.example.domain.usecase.auth.*
import com.example.domain.usecase.home.*
import com.example.domain.usecase.vocabulary.*
import com.example.domain.usecase.flashcard.*
import com.example.domain.usecase.profile.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

typealias DashboardStats = com.example.domain.model.DashboardStats
typealias DailyActivity = com.example.domain.model.DailyActivity

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class VocabularyViewModel(
    private val getUserUseCase: GetUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val signUpUseCase: SignUpUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeLoginStateUseCase: ObserveLoginStateUseCase,
    private val getDashboardStatsUseCase: GetDashboardStatsUseCase,
    private val getVocabularySetsUseCase: GetVocabularySetsUseCase,
    private val getWordsInSetUseCase: GetWordsInSetUseCase,
    private val manageVocabularySetUseCase: ManageVocabularySetUseCase,
    private val manageVocabularyWordUseCase: ManageVocabularyWordUseCase,
    private val getDueWordsUseCase: GetDueWordsUseCase,
    private val reviewWordUseCase: ReviewWordUseCase,
    private val updateEnglishLevelUseCase: UpdateEnglishLevelUseCase,
    private val exportWordsUseCase: ExportWordsUseCase,
    private val importWordsUseCase: ImportWordsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
) : ViewModel() {

    val userState: StateFlow<UserEntity?> = getUserUseCase.getFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isUserLoggedIn: StateFlow<Boolean?> = observeLoginStateUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory = _selectedCategory.asStateFlow()

    val categories: StateFlow<List<String>> = getVocabularySetsUseCase("")
        .map { sets ->
            listOf("Tất cả") + sets.map { it.category }.distinct().sorted()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Tất cả"))

    val wordSets: StateFlow<List<VocabularySetEntity>> = combine(
        _searchQuery.debounce(300),
        _selectedCategory
    ) { query, category ->
        query to category
    }.flatMapLatest { (query, category) ->
        getVocabularySetsUseCase(query).map { sets ->
            if (category == "Tất cả") sets
            else sets.filter { it.category == category }
        }
    }.flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSetId = MutableStateFlow<Int?>(null)
    val currentSetId = _currentSetId.asStateFlow()

    val currentSet: StateFlow<VocabularySetEntity?> = _currentSetId
        .flatMapLatest { id ->
            flow {
                emit(id?.let { getVocabularySetsUseCase.getById(it) })
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val wordsInCurrentSet: StateFlow<List<VocabularyWordEntity>> = _currentSetId
        .flatMapLatest { id ->
            if (id == -1) {
                // Return all words in the system for "Global Study"
                getDueWordsUseCase() 
            } else {
                getWordsInSetUseCase(id)
            }
        }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteWords: StateFlow<List<VocabularyWordEntity>> = manageVocabularyWordUseCase.getFavoriteWordsFlow()
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDueWords: StateFlow<List<VocabularyWordEntity>> = getDueWordsUseCase()
        .distinctUntilChanged()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = getDashboardStatsUseCase()
        .flowOn(Dispatchers.IO) 
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginUseCase(email, password)
        }
    }

    fun signUp(email: String, name: String, password: String, englishLevel: EnglishLevel, learningGoal: String) {
        viewModelScope.launch {
            signUpUseCase(name, email, password, englishLevel, learningGoal)
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }

    fun selectSet(setId: Int?) {
        _currentSetId.value = setId
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = category
    }

    fun addWord(setId: Int, wordTxt: String, ipa: String, meaningTxt: String, exampleTxt: String, noteTxt: String, descriptionEN: String, collocations: String, relatedWords: String) {
        viewModelScope.launch {
            manageVocabularyWordUseCase.addWord(setId, wordTxt, ipa, meaningTxt, exampleTxt, noteTxt, descriptionEN, collocations, relatedWords)
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

    fun addSet(name: String, description: String, tags: String, level: EnglishLevel = EnglishLevel.A1, category: String = "General") {
        viewModelScope.launch {
            manageVocabularySetUseCase.addSet(name, description, tags, level, category)
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

    fun updateProfile(
        name: String? = null,
        avatarUrl: String? = null,
        level: EnglishLevel? = null,
        goal: String? = null
    ) {
        viewModelScope.launch {
            updateProfileUseCase(name, avatarUrl, level, goal)
        }
    }

    fun reviewWordResponse(word: VocabularyWordEntity, rating: Int) {
        viewModelScope.launch {
            reviewWordUseCase(word, rating)
        }
    }

    fun exportWords(setId: Int, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csvData = exportWordsUseCase(setId)
            onResult(csvData)
        }
    }

    fun importWords(setId: Int, csvData: String) {
        viewModelScope.launch {
            importWordsUseCase(setId, csvData)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(VocabularyViewModel::class.java)) {
                val authRepo = ServiceLocator.provideAuthRepository(context)
                val userRepo = ServiceLocator.provideUserRepository(context)
                val setRepo = ServiceLocator.provideVocabularySetRepository(context)
                val wordRepo = ServiceLocator.provideVocabularyWordRepository(context)
                val historyRepo = ServiceLocator.provideReviewHistoryRepository(context)
                
                return VocabularyViewModel(
                    getUserUseCase = GetUserUseCase(authRepo),
                    loginUseCase = LoginUseCase(authRepo),
                    signUpUseCase = SignUpUseCase(authRepo),
                    logoutUseCase = LogoutUseCase(authRepo),
                    observeLoginStateUseCase = ObserveLoginStateUseCase(authRepo),
                    getDashboardStatsUseCase = GetDashboardStatsUseCase(userRepo, wordRepo, historyRepo),
                    getVocabularySetsUseCase = GetVocabularySetsUseCase(setRepo),
                    getWordsInSetUseCase = GetWordsInSetUseCase(wordRepo),
                    manageVocabularySetUseCase = ManageVocabularySetUseCase(setRepo),
                    manageVocabularyWordUseCase = ManageVocabularyWordUseCase(wordRepo),
                    getDueWordsUseCase = GetDueWordsUseCase(wordRepo),
                    reviewWordUseCase = ReviewWordUseCase(wordRepo, historyRepo, UpdateStreakUseCase(userRepo)),
                    updateEnglishLevelUseCase = UpdateEnglishLevelUseCase(userRepo),
                    exportWordsUseCase = ExportWordsUseCase(wordRepo),
                    importWordsUseCase = ImportWordsUseCase(wordRepo),
                    updateProfileUseCase = UpdateProfileUseCase(userRepo)
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
