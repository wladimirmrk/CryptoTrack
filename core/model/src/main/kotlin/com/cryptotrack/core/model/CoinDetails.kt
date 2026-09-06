package com.cryptotrack.core.model

import java.math.BigDecimal

/**
 * Полные данные коина для экрана Coin Details (цены в валюте отображения).
 */
data class CoinDetails(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String?,
    val description: String,
    val currentPrice: BigDecimal,
    val priceChangePercentage24h: Double,
    val marketCap: BigDecimal?,
    val marketCapRank: Int?,
    val totalVolume: BigDecimal?,
    val high24h: BigDecimal?,
    val low24h: BigDecimal?,
    val circulatingSupply: BigDecimal?,
)
