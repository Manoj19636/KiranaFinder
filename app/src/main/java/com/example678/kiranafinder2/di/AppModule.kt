package com.example678.kiranafinder2.di

import com.example678.kiranafinder2.data.repository.AuthRepositoryImpl
import com.example678.kiranafinder2.data.repository.LocationRepositoryImpl
import com.example678.kiranafinder2.data.repository.StoreRepositoryImpl
import com.example678.kiranafinder2.domain.repository.AuthRepository
import com.example678.kiranafinder2.domain.repository.LocationRepository
import com.example678.kiranafinder2.domain.repository.StoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindStoreRepository(
        storeRepositoryImpl: StoreRepositoryImpl
    ): StoreRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        locationRepositoryImpl: LocationRepositoryImpl
    ): LocationRepository
}
