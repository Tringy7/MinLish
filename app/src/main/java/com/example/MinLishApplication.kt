package com.example

import android.app.Application
import android.util.Log
import com.example.di.ServiceLocator
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VocabularySetEntity
import com.example.data.local.entity.VocabularyWordEntity
import com.example.domain.model.EnglishLevel
import com.example.domain.model.AuthProvider
import kotlinx.coroutines.*
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MinLishApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo notification channels
        NotificationHelper.createNotificationChannels(this)

        // Khởi tạo dữ liệu mẫu trong Coroutine để không block Main Thread
        applicationScope.launch(Dispatchers.IO) {
            Log.d("MinLishApplication", "Starting database seeding...")
            try {
                // Pre-warm the database connection
                ServiceLocator.provideUserRepository(this@MinLishApplication).getUser()
                seedDatabase()
            } catch (e: Exception) {
                Log.e("MinLishApplication", "Error seeding database", e)
            }
            Log.d("MinLishApplication", "Database seeding completed.")
        }
    }

    private suspend fun seedDatabase() {
        val userRepo = ServiceLocator.provideUserRepository(this)
        val setRepo = ServiceLocator.provideVocabularySetRepository(this)
        val wordRepo = ServiceLocator.provideVocabularyWordRepository(this)
        val sessionManager = ServiceLocator.getSessionManager(this)

        if (userRepo.getUser() == null) {
            val user = UserEntity(
                id = 1,
                name = "Hữu Trí Nguyễn",
                email = "huutria22005@gmail.com",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256&auto=format&fit=crop",
                englishLevel = EnglishLevel.B2,
                streakCount = 5,
                lastStudyDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000L // Studied yesterday, streak active!
            )
            userRepo.saveUser(user)
            // Initialize session for the default user
            sessionManager.saveSession(user.id, AuthProvider.LOCAL)
        }

        // Populate initial premium Vocab sets if table index 1 does not exist
        val examSet = setRepo.getSetById(1)
        if (examSet == null) {
            val setId1 = setRepo.insertSet(
                VocabularySetEntity(
                    id = 1,
                    name = "Essential TOEFL Words",
                    description = "Key vocabulary requested for high-tier academic tests (TOEFL, IELTS, SAT).",
                    tags = "TOEFL,Academic,Vocabulary",
                    isSystem = true,
                    userId = null
                )
            )

        if (setId1 != -1) {
            val words1 = listOf(
                VocabularyWordEntity(
                    setId = setId1,
                    userId = 1,
                    word = "Pragmatic",
                    pronunciation = "/præɡˈmæt.ɪk/",
                    meaning = "Thực tiễn, thực tế, giải quyết vấn đề khách quan",
                    example = "He took a pragmatic approach to solving the complex software bug.",
                    note = "Commonly tested. Synonyms: practical, realistic.",
                    descriptionEN = "Dealing with things sensibly and realistically in a way that is based on practical rather than theoretical considerations.",
                    collocations = "Pragmatic approach, pragmatic solution, pragmatic policy",
                    relatedWords = "Practical, realistic, logical, utilitarian",
                    isFavorite = true
                ),
                VocabularyWordEntity(
                    setId = setId1,
                    userId = 1,
                    word = "Eloquent",
                    pronunciation = "/ˈel.ə.kwənt/",
                    meaning = "Hùng biện, có tài ăn nói lưu loát, thuyết phục",
                    example = "She delivered an eloquent argument that persuaded the board of examiners.",
                    note = "Adjective. Noun form is 'eloquence'.",
                    descriptionEN = "Fluent or persuasive in speaking or writing.",
                    collocations = "Eloquent speaker, eloquent plea, eloquent testimony",
                    relatedWords = "Fluent, articulate, persuasive, silver-tongued",
                    isFavorite = false
                ),
                VocabularyWordEntity(
                    setId = setId1,
                    userId = 1,
                    word = "Ubiquitous",
                    pronunciation = "/juːˈbɪk.wɪ.təs/",
                    meaning = "Có mặt ở khắp mọi nơi, phổ biến rộng rãi",
                    example = "In the modern age, smartphones and mobile applications are ubiquitous.",
                    note = "Highly professional word. Synonyms: omnipresent, widespread.",
                    descriptionEN = "Present, appearing, or found everywhere.",
                    collocations = "Ubiquitous presence, ubiquitous technology, ubiquitous influence",
                    relatedWords = "Omnipresent, widespread, pervasive, ever-present",
                    isFavorite = true
                )
            )
            wordRepo.insertWords(words1)
        }

            val setId2 = setRepo.insertSet(
                VocabularySetEntity(
                    id = 2,
                    name = "Daily Conversation Idioms",
                    description = "Key idioms used by native speakers to sound organic in informal discussions.",
                    tags = "Daily,Conversational,Idiom",
                    isSystem = true,
                    userId = null
                )
            )

        if (setId2 != -1) {
            val words2 = listOf(
                VocabularyWordEntity(
                    setId = setId2,
                    userId = 1,
                    word = "Break a leg",
                    pronunciation = "/breɪk ə leɡ/",
                    meaning = "Chúc may mắn! (Nói trước khi lên sân khấu)",
                    example = "I know you can do it. Break a leg in your presentation tomorrow!",
                    note = "Never literal. Used to prevent bad luck in theatrical traditions.",
                    descriptionEN = "A superstitious way to say 'good luck' to a performer before they go on stage.",
                    collocations = "Go out there and break a leg!",
                    relatedWords = "Good luck, knock 'em dead, best of luck",
                    isFavorite = false
                ),
                VocabularyWordEntity(
                    setId = setId2,
                    userId = 1,
                    word = "Bite the bullet",
                    pronunciation = "/baɪt ðə ˈbʊl.ɪt/",
                    meaning = "Cắn răng chịu đựng, chấp nhận đối mặt khó khăn",
                    example = "I hate doing examinations, but I just have to bite the bullet.",
                    note = "Derived from soldiers biting bullets during battlefield medicine.",
                    descriptionEN = "To endure a painful or otherwise unpleasant situation that is seen as unavoidable.",
                    collocations = "Decide to bite the bullet, finally bite the bullet",
                    relatedWords = "Face the music, tough it out, grin and bear it",
                    isFavorite = true
                )
            )
            wordRepo.insertWords(words2)
        }
        }

        // Force Database access on startup to keep connection open for Inspector
        try {
            userRepo.getUser()
            Log.d("MinLishApplication", "Database initialized and opened successfully")
        } catch (e: Exception) {
            Log.e("MinLishApplication", "Failed to open database: ${e.message}")
        }
    }
}
