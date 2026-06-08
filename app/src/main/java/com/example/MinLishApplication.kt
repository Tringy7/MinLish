package com.example

import android.app.Application
import android.util.Log
import com.example.di.ServiceLocator
import com.example.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MinLishApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Khởi tạo notification channels
        NotificationHelper.createNotificationChannels(this)

        // Pre-warm the database connection and log status
        applicationScope.launch(Dispatchers.IO) {
            try {
                ServiceLocator.provideUserRepository(this@MinLishApplication).getUser()
                Log.d("MinLishApplication", "Database initialized and opened successfully")
            } catch (e: Exception) {
                Log.e("MinLishApplication", "Failed to open database: ${e.message}")
            }
        }
    }
}
