package com.cryptotrack.core.data.database.entity

import androidx.room.Entity

/** Кэш деталей коина, per-валюта. */
@Entity(tableName = "coin_details", primaryKeys = ["coinId", "currency"])
data class CoinDetailsEntity(
    val coinId: String,
    val currency: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val description: String,
    val currentPrice: String,
    val changePercent24h: Double,
    val marketCap: String?,
    val marketCapRank: Int?,
    val totalVolume: String?,
    val high24h: String?,
    val low24h: String?,
    val circulatingSupply: String?,
    val fetchedAtMillis: Long,
)
