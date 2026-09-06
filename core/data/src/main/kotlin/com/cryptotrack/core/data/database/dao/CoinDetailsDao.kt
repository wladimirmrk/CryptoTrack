package com.cryptotrack.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cryptotrack.core.data.database.entity.CoinDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDetailsDao {
    @Query("SELECT * FROM coin_details WHERE coinId = :coinId AND currency = :currency")
    fun observe(
        coinId: String,
        currency: String,
    ): Flow<CoinDetailsEntity?>

    @Query("SELECT * FROM coin_details WHERE coinId = :coinId AND currency = :currency")
    suspend fun get(
        coinId: String,
        currency: String,
    ): CoinDetailsEntity?

    @Upsert
    suspend fun upsert(details: CoinDetailsEntity)
}
