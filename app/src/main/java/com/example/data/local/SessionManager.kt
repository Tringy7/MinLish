package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.AuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class SessionManager(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val CURRENT_USER_ID = intPreferencesKey("current_user_id")
        private val CURRENT_PROVIDER = stringPreferencesKey("current_provider")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val currentUserId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID]?.takeIf { it > 0 }
    }

    val currentProvider: Flow<AuthProvider?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_PROVIDER]?.takeIf { it.isNotBlank() }?.let { 
            try { AuthProvider.valueOf(it) } catch (e: Exception) { null }
        }
    }

    suspend fun saveSession(userId: Int, provider: AuthProvider) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[CURRENT_USER_ID] = userId
            preferences[CURRENT_PROVIDER] = provider.name
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
