package com.example.petsocial.core.datastore.di

import android.content.Context
import com.example.petsocial.core.datastore.auth.DataStoreTokenStorage
import com.example.petsocial.core.datastore.auth.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideTokenStorage(
        @ApplicationContext context: Context
    ): TokenStorage {
        return DataStoreTokenStorage(context)
    }
}