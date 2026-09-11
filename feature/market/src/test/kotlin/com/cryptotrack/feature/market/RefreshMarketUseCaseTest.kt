package com.cryptotrack.feature.market

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.market.domain.RefreshMarketUseCase
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * TTL маркет-листа (60s, architecture.md §6): без force сетевой вызов делается
 * только если кэш старше TTL; force (pull-to-refresh) обновляет всегда.
 * Use case читает System.currentTimeMillis напрямую — свежесть задаётся
 * значением lastUpdated в фейке.
 */
class RefreshMarketUseCaseTest {
    private val repository = TtlFakeMarketRepository()
    private val settings = TtlFakeSettingsRepository()
    private val useCase = RefreshMarketUseCase(repository, settings)

    @Test
    fun `fresh cache skips refresh without force`() =
        runTest {
            repository.lastUpdated.value = System.currentTimeMillis()

            val result = useCase()

            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat(repository.refreshCalls).isEqualTo(0)
        }

    @Test
    fun `stale cache triggers refresh without force`() =
        runTest {
            repository.lastUpdated.value = 0L // возраст >> TTL

            val result = useCase()

            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat(repository.refreshCalls).isEqualTo(1)
            assertThat(repository.lastCurrency).isEqualTo("usd")
        }

    @Test
    fun `missing timestamp triggers refresh`() =
        runTest {
            repository.lastUpdated.value = null

            useCase()

            assertThat(repository.refreshCalls).isEqualTo(1)
        }

    @Test
    fun `force refresh hits network even with fresh cache`() =
        runTest {
            repository.lastUpdated.value = System.currentTimeMillis()

            useCase(force = true)

            assertThat(repository.refreshCalls).isEqualTo(1)
        }

    @Test
    fun `refresh failure propagates to caller`() =
        runTest {
            repository.lastUpdated.value = 0L
            repository.refreshResult = Result.Failure(AppError.RateLimit)

            val result = useCase()

            assertThat((result as Result.Failure).error).isEqualTo(AppError.RateLimit)
        }
}

private class TtlFakeMarketRepository : MarketRepository {
    val lastUpdated = MutableStateFlow<Long?>(null)
    var refreshResult: Result<Unit> = Result.Success(Unit)
    var refreshCalls = 0
    var lastCurrency: String? = null

    override fun observeMarkets(currency: String): Flow<List<Coin>> = MutableStateFlow(emptyList())

    override suspend fun refreshMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ): Result<Unit> {
        refreshCalls++
        lastCurrency = currency
        return refreshResult
    }

    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = lastUpdated

    override fun observeCoin(
        coinId: String,
        currency: String,
    ): Flow<Coin?> = MutableStateFlow(null)

    override suspend fun getCoin(
        coinId: String,
        currency: String,
    ): Coin? = null

    override fun observeCoinDetails(
        coinId: String,
        currency: String,
    ): Flow<com.cryptotrack.core.model.CoinDetails?> = MutableStateFlow(null)

    override suspend fun refreshCoinDetails(
        coinId: String,
        currency: String,
    ): Result<Unit> = throw NotImplementedError()

    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Flow<List<com.cryptotrack.core.model.PricePoint>> = MutableStateFlow(emptyList())

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Result<Unit> = throw NotImplementedError()

    override suspend fun searchCoins(
        query: String,
        currency: String,
    ): Result<List<Coin>> = throw NotImplementedError()

    override suspend fun pingApi(): Result<Long> = throw NotImplementedError()
}

private class TtlFakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())

    override suspend fun setCurrency(currency: Currency) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setDarkMode(enabled: Boolean) = Unit

    override suspend fun setRefreshInterval(interval: RefreshInterval) {
        settings.value = settings.value.copy(refreshInterval = interval)
    }
}
