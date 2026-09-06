package com.cryptotrack.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cryptotrack.core.data.database.entity.MarketCoinEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {
    @Query("SELECT * FROM market_coins WHERE currency = :currency")
    fun observeAll(currency: String): Flow<List<MarketCoinEntity>>

    @Query("SELECT * FROM market_coins WHERE currency = :currency AND coinId = :coinId")
    suspend fun get(
        coinId: String,
        currency: String,
    ): MarketCoinEntity?

    @Upsert
    suspend fun upsertAll(coins: List<MarketCoinEntity>)
}
