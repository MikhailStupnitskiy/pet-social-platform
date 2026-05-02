package com.example.petsocial.di

import com.example.petsocial.core.common.session.SessionEventBus
import com.example.petsocial.session.DefaultSessionEventBus
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionEventModule {

    @Binds
    @Singleton
    abstract fun bindSessionEventBus(
        impl: DefaultSessionEventBus
    ): SessionEventBus
}