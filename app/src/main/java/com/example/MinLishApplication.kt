package com.example

import android.app.Application
import com.example.di.ServiceLocator
import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.VocabularySetEntity
import com.example.data.local.entity.VocabularyWordEntity
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MinLishApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Khởi tạo notification channels
        NotificationHelper.createNotificationChannels(this)

        // Khởi tạo dữ liệu mẫu trong Coroutine để không block Main Thread
        // Thêm delay nhẹ hoặc yield để nhường CPU cho quá trình render frame đầu tiên của UI
        // Force Database access on startup to keep connection open for Inspector
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userRepo = ServiceLocator.provideUserRepository(this@MinLishApplication)
                // Accessing any DAO method opens the connection
                userRepo.getUser()
                android.util.Log.d("MinLish", "Database initialized and opened successfully")
            } catch (e: Exception) {
                android.util.Log.e("MinLish", "Failed to open database: ${e.message}")
            }
        }
    }
}
