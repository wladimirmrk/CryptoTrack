package com.cryptotrack.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cryptotrack.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAtMillis DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT coinId FROM favorites")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE coinId = :coinId)")
    fun observeIsFavorite(coinId: String): Flow<Boolean>

    @Upsert
    suspend fun add(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE coinId = :coinId")
    suspend fun remove(coinId: String)
}
