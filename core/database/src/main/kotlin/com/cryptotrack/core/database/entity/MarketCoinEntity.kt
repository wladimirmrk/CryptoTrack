package com.cryptotrack.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Кэш маркет-списка, per-валюта (ADR-005: Room — единственный источник для UI).
 * Цены хранятся строками BigDecimal в валюте [currency].
 */
@Entity(
    tableName = "market_coins",
    primaryKeys = ["coinId", "currency"],
    indices = [Index("currency")],
)
data class MarketCoinEntity(
    val coinId: String,
    val currency: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val currentPrice: String,
    val changePercent24h: Double,
    val marketCap: String?,
    val marketCapRank: Int?,
    val totalVolume: String?,
    val high24h: String?,
    val low24h: String?,
    val circulatingSupply: String?,
)
