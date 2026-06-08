package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_auth")

class AuthManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val CURRENT_USER_ID = intPreferencesKey("current_user_id")
        private val CURRENT_PROVIDER = stringPreferencesKey("current_provider")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN] }
    
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN] }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[ACCESS_TOKEN].isNullOrBlank()
    }

    val currentUserId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID]?.takeIf { it > 0 }
    }

    val currentProvider: Flow<AuthProvider?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_PROVIDER]?.takeIf { it.isNotBlank() }?.let { 
            try { AuthProvider.valueOf(it) } catch (e: Exception) { null }
        }
    }

    suspend fun saveAuthData(userId: Int, provider: AuthProvider, accessToken: String, refreshToken: String = "") {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID] = userId
            preferences[CURRENT_PROVIDER] = provider.name
            preferences[ACCESS_TOKEN] = accessToken
            preferences[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { it.clear() }
    }
}
