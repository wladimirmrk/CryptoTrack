package com.cryptotrack.core.data.di

import android.content.Context
import androidx.room.Room
import com.cryptotrack.core.data.database.CryptoTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * В Hilt-поверхности только CryptoTrackDatabase: Room 2.8 стал KMP-артефактом,
 * и аннотированные @Dao/@Entity типы не резолвятся Dagger-KSP резолвером (ADR-016).
 * Репозитории получают БД и достают DAO внутри.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): CryptoTrackDatabase =
        Room
            .databaseBuilder(context, CryptoTrackDatabase::class.java, CryptoTrackDatabase.NAME)
            .build()
}
