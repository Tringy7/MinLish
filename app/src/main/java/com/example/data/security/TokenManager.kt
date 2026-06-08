package com.example.data.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class TokenManager(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val PROVIDER_KEY = stringPreferencesKey("provider")
    }

    val accessToken: Flow<String?> = context.dataStore.data.map { it[ACCESS_TOKEN_KEY] }
    val refreshToken: Flow<String?> = context.dataStore.data.map { it[REFRESH_TOKEN_KEY] }
    val userId: Flow<Int?> = context.dataStore.data.map { it[USER_ID_KEY] }
    val provider: Flow<AuthProvider?> = context.dataStore.data.map { preferences ->
        preferences[PROVIDER_KEY]?.let { try { AuthProvider.valueOf(it) } catch (e: Exception) { null } }
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String, userId: Int, provider: AuthProvider) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
            preferences[PROVIDER_KEY] = provider.name
        }
    }

    suspend fun getAccessToken(): String? = accessToken.first()
    suspend fun getRefreshToken(): String? = refreshToken.first()

    suspend fun clearTokens() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
            preferences.remove(USER_ID_KEY)
        }
    }
}