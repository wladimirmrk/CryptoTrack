package com.cryptotrack.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.cryptotrack.core.data.database.entity.PriceHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class PriceHistoryDao {
    @Query(
        "SELECT * FROM price_history " +
            "WHERE coinId = :coinId AND currency = :currency AND range = :range " +
            "ORDER BY timestampMillis ASC",
    )
    abstract fun observe(
        coinId: String,
        currency: String,
        range: String,
    ): Flow<List<PriceHistoryEntity>>

    @Query(
        "DELETE FROM price_history " +
            "WHERE coinId = :coinId AND currency = :currency AND range = :range",
    )
    abstract suspend fun clear(
        coinId: String,
        currency: String,
        range: String,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertAll(points: List<PriceHistoryEntity>)

    /** Полная замена диапазона транзакционно, чтобы UI не видел частичную историю. */
    @Transaction
    open suspend fun replaceAll(
        coinId: String,
        currency: String,
        range: String,
        points: List<PriceHistoryEntity>,
    ) {
        clear(coinId, currency, range)
        insertAll(points)
    }
}
