package com.example.petsocial.core.network.di

import com.example.petsocial.core.datastore.auth.TokenStorage
import com.example.petsocial.core.network.API_BASE_URL
import com.example.petsocial.core.network.interceptor.AuthInterceptor
import com.example.petsocial.core.network.api.ProfileApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.example.petsocial.core.network.api.PetsApi
import com.example.petsocial.core.network.api.MatchingApi
import com.example.petsocial.core.network.api.ChatsApi
import com.example.petsocial.core.network.api.FeedApi
import com.example.petsocial.core.network.api.HandlersApi
import com.example.petsocial.core.network.api.ImagesApi
import com.example.petsocial.core.network.api.RoutineApi
import com.example.petsocial.core.network.api.NotificationsApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenStorage: TokenStorage
    ): AuthInterceptor {
        return AuthInterceptor(tokenStorage)
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }


    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }


    @Provides
    @Singleton
    fun providePetsApi(retrofit: Retrofit): PetsApi {
        return retrofit.create(PetsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMatchingApi(retrofit: Retrofit): MatchingApi {
        return retrofit.create(MatchingApi::class.java)
    }

    @Provides
    @Singleton
    fun provideChatsApi(retrofit: Retrofit): ChatsApi {
        return retrofit.create(ChatsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRoutineApi(retrofit: Retrofit): RoutineApi {
        return retrofit.create(RoutineApi::class.java)
    }

    @Provides
    @Singleton
    fun provideFeedApi(retrofit: Retrofit): FeedApi {
        return retrofit.create(FeedApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImagesApi(retrofit: Retrofit): ImagesApi {
        return retrofit.create(ImagesApi::class.java)
    }

    @Provides
    @Singleton
    fun provideHandlersApi(retrofit: Retrofit): HandlersApi {
        return retrofit.create(HandlersApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationsApi(retrofit: Retrofit): NotificationsApi {
        return retrofit.create(NotificationsApi::class.java)
    }
}
