package com.example.petsocial.feature.pets

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PetsModule {

    @Binds
    @Singleton
    abstract fun bindPetsRepository(
        impl: DefaultPetsRepository
    ): PetsRepository
}