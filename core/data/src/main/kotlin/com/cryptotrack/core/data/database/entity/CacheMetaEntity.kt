package com.cryptotrack.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Метаданные кэша: время последнего успешного обновления (offline-баннер «Last updated»). */
@Entity(tableName = "cache_meta")
data class CacheMetaEntity(
    @PrimaryKey val key: String,
    val lastUpdatedMillis: Long,
)
