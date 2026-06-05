package com.example

import android.app.Application
import com.example.di.ServiceLocator
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MinLishApplication : Application() {
    override fun onCreate() {
        super.onCreate()
//
//        // Initialize only essential data.
//        // 7000+ words are now handled by Prepackaged Database in AppDatabase.
//        CoroutineScope(Dispatchers.IO).launch {
//            val userRepo = ServiceLocator.provideUserRepository(this@MinLishApplication)
//
//            val user = userRepo.getUser()
//            if (user == null) {
//                val newUser = UserEntity(
//                    email = "huutria22005@gmail.com",
//                    name = "Hữu Trí Nguyễn",
//                    passwordHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", // hash of '123456'
//                    avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=256&auto=format&fit=crop",
//                    englishLevel = "B2 - Upper Intermediate",
//                    streakCount = 5,
//                    lastStudyDate = System.currentTimeMillis() - 24 * 60 * 60 * 1000L,
//                    totalXp = 1500
//                )
//                userRepo.saveUser(newUser)
//            }
//        }
    }
}
