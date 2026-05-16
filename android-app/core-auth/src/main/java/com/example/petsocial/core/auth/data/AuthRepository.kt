package com.example.petsocial.core.auth.data

import com.example.petsocial.core.auth.model.UserResponse

interface AuthRepository {

    suspend fun login(email: String, password: String): UserResponse

    suspend fun register(email: String, password: String, isHandler: Boolean = false): UserResponse

    suspend fun getMe(): UserResponse

    suspend fun logout()
}
