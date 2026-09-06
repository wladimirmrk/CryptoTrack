package com.cryptotrack.core.data.di

import com.cryptotrack.core.data.impl.FavoritesRepositoryImpl
import com.cryptotrack.core.data.impl.MarketRepositoryImpl
import com.cryptotrack.core.data.impl.PortfolioRepositoryImpl
import com.cryptotrack.core.data.impl.SettingsRepositoryImpl
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMarketRepository(impl: MarketRepositoryImpl): MarketRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
