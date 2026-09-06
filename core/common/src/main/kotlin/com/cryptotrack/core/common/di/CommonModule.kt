package com.cryptotrack.core.common.di

import com.cryptotrack.core.common.coroutines.DispatchersProvider
import com.cryptotrack.core.common.coroutines.StandardDispatchers
import com.cryptotrack.core.common.time.SystemTimeProvider
import com.cryptotrack.core.common.time.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Биндинги общих провайдеров (architecture.md §7: модуль поставляет DI рядом с реализациями). */
@Module
@InstallIn(SingletonComponent::class)
internal object CommonModule {

    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = SystemTimeProvider()

    @Provides
    @Singleton
    fun provideDispatchersProvider(): DispatchersProvider = StandardDispatchers()
}
