package com.cryptotrack.core.network.source

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.common.result.map
import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.network.CoinGeckoApi
import com.cryptotrack.core.network.error.safeApiCall
import com.cryptotrack.core.network.mapper.toCoin
import com.cryptotrack.core.network.mapper.toCoinDetails
import com.cryptotrack.core.network.mapper.toPricePoints
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinGeckoRemoteDataSource
    @Inject
    constructor(
        private val api: CoinGeckoApi,
        private val timeProvider: TimeProvider,
    ) : MarketRemoteDataSource {
        override suspend fun getMarkets(
            currency: String,
            page: Int,
            perPage: Int,
        ): Result<List<Coin>> =
            safeApiCall { api.getMarkets(currency = currency, perPage = perPage, page = page) }
                .map { list -> list.map { it.toCoin() } }

        override suspend fun getCoinDetails(
            coinId: String,
            currency: String,
        ): Result<CoinDetails> =
            safeApiCall { api.getCoinDetails(coinId = coinId) }
                .map { it.toCoinDetails(currency) }

        override suspend fun getMarketChart(
            coinId: String,
            currency: String,
            range: ChartRange,
        ): Result<List<PricePoint>> =
            safeApiCall {
                api.getMarketChart(coinId = coinId, currency = currency, days = range.apiDays)
            }.map { dto ->
                val points = dto.toPricePoints()
                if (range == ChartRange.HOUR_1) {
                    // CoinGecko не отдаёт отдельный «1H» диапазон: режем суточные данные до часа.
                    val hourAgo = timeProvider.nowMillis() - HOUR_MILLIS
                    points.filter { it.timestampMillis >= hourAgo }
                } else {
                    points
                }
            }

        override suspend fun search(query: String): Result<List<Coin>> =
            safeApiCall { api.search(query = query) }
                .map { dto -> dto.coins.map { it.toCoin() } }

        override suspend fun ping(): Result<Long> =
            safeApiCall {
                val started = timeProvider.nowMillis()
                api.ping()
                timeProvider.nowMillis() - started
            }

        private companion object {
            const val HOUR_MILLIS = 60L * 60L * 1000L
        }
    }
