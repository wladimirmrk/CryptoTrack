package com.cryptotrack.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Избранный коин (звезда в Market/Search/Coin Details). */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val coinId: String,
    val addedAtMillis: Long,
)
