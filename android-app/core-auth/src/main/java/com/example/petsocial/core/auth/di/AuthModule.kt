package com.example.petsocial.core.auth.di

import com.example.petsocial.core.auth.api.AuthApi
import com.example.petsocial.core.auth.data.AuthRepository
import com.example.petsocial.core.auth.data.DefaultAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: DefaultAuthRepository
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}