package com.cryptotrack.core.network.source

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.PricePoint

/**
 * Абстракция провайдера рыночных данных (ADR-006): домен и репозитории
 * не знают о CoinGecko. Реализация по умолчанию — CoinGeckoRemoteDataSource.
 */
interface MarketRemoteDataSource {
    suspend fun getMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ): Result<List<Coin>>

    suspend fun getCoinDetails(
        coinId: String,
        currency: String,
    ): Result<CoinDetails>

    suspend fun getMarketChart(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Result<List<PricePoint>>

    suspend fun search(query: String): Result<List<Coin>>

    /** Проверка соединения (макет Settings: «Connected (Latency: 42ms)»). Возвращает RTT в мс. */
    suspend fun ping(): Result<Long>
}
