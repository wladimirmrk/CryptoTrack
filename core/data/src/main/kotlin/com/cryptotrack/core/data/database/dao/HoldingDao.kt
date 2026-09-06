package com.cryptotrack.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cryptotrack.core.data.database.entity.HoldingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HoldingDao {

    @Query("SELECT * FROM holdings ORDER BY updatedAtMillis DESC")
    fun observeAll(): Flow<List<HoldingEntity>>

    @Query("SELECT * FROM holdings WHERE coinId = :coinId")
    fun observe(coinId: String): Flow<HoldingEntity?>

    @Query("SELECT * FROM holdings WHERE coinId = :coinId")
    suspend fun get(coinId: String): HoldingEntity?

    @Upsert
    suspend fun upsert(holding: HoldingEntity)

    @Query("DELETE FROM holdings WHERE coinId = :coinId")
    suspend fun delete(coinId: String)
}
