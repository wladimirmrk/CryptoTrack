package com.cryptotrack.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cryptotrack.core.database.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceHistoryDao {

    @Query(
        "SELECT * FROM price_history " +
            "WHERE coinId = :coinId AND currency = :currency AND range = :range " +
            "ORDER BY timestampMillis ASC",
    )
    fun observe(coinId: String, currency: String, range: String): Flow<List<PriceHistoryEntity>>

    /** Полная замена диапазона: история перезаписывается свежим ответом. */
    @Query(
        "DELETE FROM price_history " +
            "WHERE coinId = :coinId AND currency = :currency AND range = :range",
    )
    suspend fun clear(coinId: String, currency: String, range: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(points: List<PriceHistoryEntity>)
}
