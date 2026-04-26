package com.example.petsocial.core.datastore.auth

import kotlinx.coroutines.flow.Flow

interface TokenStorage {

    val token: Flow<String?>

    suspend fun saveToken(token: String)

    suspend fun clearToken()
}