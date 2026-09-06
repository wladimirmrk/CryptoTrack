package com.cryptotrack.core.data.repository

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.PricePoint
import kotlinx.coroutines.flow.Flow

/**
 * Маркет-данные: offline-first (ADR-005) — UI читает Flow из Room, сеть обновляет кэш.
 */
interface MarketRepository {

    fun observeMarkets(currency: String): Flow<List<Coin>>

    suspend fun refreshMarkets(
        currency: String,
        page: Int = 1,
        perPage: Int = DEFAULT_PER_PAGE,
    ): Result<Unit>

    fun observeMarketLastUpdated(currency: String): Flow<Long?>

    fun observeCoin(coinId: String, currency: String): Flow<Coin?>

    suspend fun getCoin(coinId: String, currency: String): Coin?

    fun observeCoinDetails(coinId: String, currency: String): Flow<CoinDetails?>

    suspend fun refreshCoinDetails(coinId: String, currency: String): Result<Unit>

    fun observePriceHistory(coinId: String, currency: String, range: ChartRange): Flow<List<PricePoint>>

    suspend fun refreshPriceHistory(coinId: String, currency: String, range: ChartRange): Result<Unit>

    /** Поиск по CoinGecko с обогащением ценами из кэша, если коин уже в нём. */
    suspend fun searchCoins(query: String, currency: String): Result<List<Coin>>

    suspend fun pingApi(): Result<Long>

    companion object {
        const val DEFAULT_PER_PAGE = 50
    }
}
