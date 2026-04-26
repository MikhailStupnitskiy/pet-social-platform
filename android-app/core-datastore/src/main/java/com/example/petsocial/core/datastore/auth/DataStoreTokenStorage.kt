package com.example.petsocial.core.datastore.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "auth_preferences")

class DataStoreTokenStorage(
    private val context: Context
) : TokenStorage {

    override val token: Flow<String?> =
        context.authDataStore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }

    override suspend fun saveToken(token: String) {
        context.authDataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    override suspend fun clearToken() {
        context.authDataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    private companion object {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
    }
}