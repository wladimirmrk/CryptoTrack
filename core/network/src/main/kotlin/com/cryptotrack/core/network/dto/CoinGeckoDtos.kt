package com.cryptotrack.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class MarketCoinDto(
    val id: String,
    val symbol: String = "",
    val name: String = "",
    val image: String? = null,
    val current_price: Double? = null,
    val market_cap: Double? = null,
    val market_cap_rank: Int? = null,
    val total_volume: Double? = null,
    val high_24h: Double? = null,
    val low_24h: Double? = null,
    val circulating_supply: Double? = null,
    @kotlinx.serialization.SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
)

@Serializable
data class CoinImageDto(
    val large: String? = null,
)

@Serializable
data class CoinDescriptionDto(
    val en: String? = null,
)

@Serializable
data class CoinMarketDataDto(
    val current_price: Map<String, Double> = emptyMap(),
    val market_cap: Map<String, Double> = emptyMap(),
    val total_volume: Map<String, Double> = emptyMap(),
    val high_24h: Map<String, Double> = emptyMap(),
    val low_24h: Map<String, Double> = emptyMap(),
    val circulating_supply: Double? = null,
    @kotlinx.serialization.SerialName("market_cap_rank")
    val marketCapRank: Int? = null,
    @kotlinx.serialization.SerialName("price_change_percentage_24h")
    val priceChangePercentage24h: Double? = null,
)

@Serializable
data class CoinDetailsDto(
    val id: String,
    val symbol: String = "",
    val name: String = "",
    val image: CoinImageDto? = null,
    val description: CoinDescriptionDto? = null,
    val market_data: CoinMarketDataDto? = null,
)

@Serializable
data class MarketChartDto(
    val prices: List<List<Double>> = emptyList(),
)

@Serializable
data class SearchCoinDto(
    val id: String,
    val name: String = "",
    val symbol: String = "",
    val thumb: String? = null,
    val large: String? = null,
    @kotlinx.serialization.SerialName("market_cap_rank")
    val marketCapRank: Int? = null,
)

@Serializable
data class SearchDto(
    val coins: List<SearchCoinDto> = emptyList(),
)

@Serializable
data class PingDto(
    val gecko_says: String? = null,
)
