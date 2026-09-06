package com.cryptotrack.core.network

import com.cryptotrack.core.network.dto.CoinDetailsDto
import com.cryptotrack.core.network.dto.MarketChartDto
import com.cryptotrack.core.network.dto.MarketCoinDto
import com.cryptotrack.core.network.dto.PingDto
import com.cryptotrack.core.network.dto.SearchDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * CoinGecko API v3 (ADR-006). Замена провайдера = новая реализация MarketRemoteDataSource.
 */
interface CoinGeckoApi {
    @GET("coins/markets")
    suspend fun getMarkets(
        @Query("vs_currency") currency: String,
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int,
        @Query("page") page: Int,
        @Query("price_change_percentage") priceChangePercentage: String = "24h",
        @Query("sparkline") sparkline: Boolean = false,
    ): List<MarketCoinDto>

    @GET("coins/{id}")
    suspend fun getCoinDetails(
        @Path("id") coinId: String,
        @Query("localization") localization: String = "en",
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
    ): CoinDetailsDto

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") coinId: String,
        @Query("vs_currency") currency: String,
        @Query("days") days: String,
    ): MarketChartDto

    @GET("search")
    suspend fun search(
        @Query("query") query: String,
    ): SearchDto

    @GET("ping")
    suspend fun ping(): PingDto
}
