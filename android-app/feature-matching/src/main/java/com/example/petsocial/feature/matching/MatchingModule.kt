package com.example.petsocial.feature.matching

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchingModule {

    @Binds
    @Singleton
    abstract fun bindMatchingRepository(
        impl: DefaultMatchingRepository
    ): MatchingRepository
}