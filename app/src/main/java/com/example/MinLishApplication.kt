package com.example

import android.app.Application
import com.example.di.ServiceLocator
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VocabularySetEntity
import com.example.data.local.entity.VocabularyWordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MinLishApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Khởi tạo dữ liệu mẫu trong Coroutine để không block Main Thread
        // Thêm delay nhẹ hoặc yield để nhường CPU cho quá trình render frame đầu tiên của UI
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(500) // Delay 500ms để UI render xong frame đầu tiên
            val userRepo = ServiceLocator.provideUserRepository(this@MinLishApplication)
            val setRepo = ServiceLocator.provideVocabularySetRepository(this@MinLishApplication)
            val wordRepo = ServiceLocator.provideVocabularyWordRepository(this@MinLishApplication)

            if (userRepo.getUser() == null) {
                userRepo.saveUser(
                    UserEntity(
                        id = "local_user",
                        name = "Hữu Trí Nguyễn",
                        email = "huutria22005@gmail.com",
                        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256&auto=format&fit=crop",
                        englishLevel = "B2 - Upper Intermediate",
                        streakCount = 5,
                        lastStudyDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000L // Studied yesterday, streak active!
                    )
                )
            }

            // Populate initial premium Vocab sets if table index 1 does not exist
            val examSet = setRepo.getSetById(1)
            if (examSet == null) {
                val setId1 = setRepo.insertSet(
                    VocabularySetEntity(
                        id = 1,
                        name = "Essential TOEFL Words",
                        description = "Key vocabulary requested for high-tier academic tests (TOEFL, IELTS, SAT).",
                        tags = "TOEFL,Academic,Vocabulary"
                    )
                )
                
                wordRepo.insertWord(
                    VocabularyWordEntity(
                        setId = setId1,
                        word = "Pragmatic",
                        pronunciation = "/præɡˈmæt.ɪk/",
                        meaning = "Thực tiễn, thực tế, giải quyết vấn đề khách quan",
                        example = "He took a pragmatic approach to solving the complex software bug.",
                        note = "Commonly tested. Synonyms: practical, realistic.",
                        isFavorite = true
                    )
                )
                wordRepo.insertWord(
                    VocabularyWordEntity(
                        setId = setId1,
                        word = "Eloquent",
                        pronunciation = "/ˈel.ə.kwənt/",
                        meaning = "Hùng biện, có tài ăn nói lưu loát, thuyết phục",
                        example = "She delivered an eloquent argument that persuaded the board of examiners.",
                        note = "Adjective. Noun form is 'eloquence'.",
                        isFavorite = false
                    )
                )
                wordRepo.insertWord(
                    VocabularyWordEntity(
                        setId = setId1,
                        word = "Ubiquitous",
                        pronunciation = "/juːˈbɪk.wɪ.təs/",
                        meaning = "Có mặt ở khắp mọi nơi, phổ biến rộng rãi",
                        example = "In the modern age, smartphones and mobile applications are ubiquitous.",
                        note = "Highly professional word. Synonyms: omnipresent, widespread.",
                        isFavorite = true
                    )
                )

                val setId2 = setRepo.insertSet(
                    VocabularySetEntity(
                        id = 2,
                        name = "Daily Conversation Idioms",
                        description = "Key idioms used by native speakers to sound organic in informal discussions.",
                        tags = "Daily,Conversational,Idiom"
                    )
                )

                wordRepo.insertWord(
                    VocabularyWordEntity(
                        setId = setId2,
                        word = "Break a leg",
                        pronunciation = "/breɪk ə leɡ/",
                        meaning = "Chúc may mắn! (Nói trước khi lên sân khấu)",
                        example = "I know you can do it. Break a leg in your presentation tomorrow!",
                        note = "Never literal. Used to prevent bad luck in theatrical traditions.",
                        isFavorite = false
                    )
                )
                wordRepo.insertWord(
                    VocabularyWordEntity(
                        setId = setId2,
                        word = "Bite the bullet",
                        pronunciation = "/baɪt ðə ˈbʊl.ɪt/",
                        meaning = "Cắn răng chịu đựng, chấp nhận đối mặt khó khăn",
                        example = "I hate doing examinations, but I just have to bite the bullet.",
                        note = "Derived from soldiers biting bullets during battlefield medicine.",
                        isFavorite = true
                    )
                )
            }
        }
    }
}
