package com.cryptotrack.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.cryptotrack.core.database.entity.CacheMetaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CacheMetaDao {

    @Query("SELECT * FROM cache_meta WHERE `key` = :key")
    fun observe(key: String): Flow<CacheMetaEntity?>

    @Query("SELECT * FROM cache_meta WHERE `key` = :key")
    suspend fun get(key: String): CacheMetaEntity?

    @Upsert
    suspend fun put(meta: CacheMetaEntity)
}
