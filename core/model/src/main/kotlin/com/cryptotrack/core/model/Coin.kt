package com.cryptotrack.core.model

import java.math.BigDecimal

/**
 * Коин из маркет-списка / поиска / избранного.
 * Точность денег — BigDecimal (ADR-011: без потерь на процентах и микро-ценах).
 */
data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val currentPrice: BigDecimal,
    val priceChangePercentage24h: Double,
    val marketCap: BigDecimal?,
    val marketCapRank: Int?,
    val totalVolume: BigDecimal?,
    val high24h: BigDecimal?,
    val low24h: BigDecimal?,
    val circulatingSupply: BigDecimal?,
)
