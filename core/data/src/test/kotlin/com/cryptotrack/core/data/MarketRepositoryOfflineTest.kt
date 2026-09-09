package com.cryptotrack.core.data

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.database.entity.MarketCoinEntity
import com.cryptotrack.core.data.database.entity.PriceHistoryEntity
import com.cryptotrack.core.data.impl.MarketRepositoryImpl
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.network.source.MarketRemoteDataSource
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

/**
 * Offline-first сценарии (ADR-005): кэш Room — единственный источник UI,
 * сеть только перезаписывает кэш. TTL маркет-листа живёт в RefreshMarketUseCase
 * (:feature:market) — репозиторий обновляет кэш безусловно, observe от TTL не зависит.
 */
class MarketRepositoryOfflineTest {
    private val remote = OfflineFakeRemote()
    private val marketDao = InMemoryMarketDao()
    private val historyDao = InMemoryPriceHistoryDao()
    private val cacheMetaDao = InMemoryCacheMetaDao()
    private val timeProvider = FakeTimeProvider(now = 1_000L)
    private val repository =
        MarketRepositoryImpl(
            remoteDataSource = remote,
            database = TestDatabase(market = marketDao, priceHistory = historyDao, cacheMeta = cacheMetaDao),
            dispatchers = FakeDispatchers(),
            timeProvider = timeProvider,
        )

    @Test
    fun `refreshMarkets overwrites stale cache and advances last updated`() =
        runTest {
            remote.marketsResult = Result.Success(listOf(offlineCoin(id = "bitcoin", price = "10000")))
            repository.refreshMarkets("usd")
            assertThat(repository.observeMarketLastUpdated("usd").first()).isEqualTo(1_000L)

            timeProvider.now = 61_000L
            remote.marketsResult = Result.Success(listOf(offlineCoin(id = "bitcoin", price = "20000")))
            repository.refreshMarkets("usd")

            val coins = repository.observeMarkets("usd").first()
            assertThat(coins.single().currentPrice.toPlainString()).isEqualTo("20000")
            assertThat(repository.observeMarketLastUpdated("usd").first()).isEqualTo(61_000L)
        }

    @Test
    fun `observeMarkets serves cache without any refresh`() =
        runTest {
            marketDao.upsertAll(listOf(cachedEntity("cached", price = "1.00")))

            val coins = repository.observeMarkets("usd").first()

            assertThat(coins.single().id).isEqualTo("cached")
            assertThat(repository.observeMarketLastUpdated("usd").first()).isNull()
        }

    @Test
    fun `offline without cache leaves market empty after failed refresh`() =
        runTest {
            remote.marketsResult = Result.Failure(AppError.Network)

            val result = repository.refreshMarkets("usd")

            assertThat((result as Result.Failure).error).isEqualTo(AppError.Network)
            assertThat(repository.observeMarkets("usd").first()).isEmpty()
            assertThat(repository.observeMarketLastUpdated("usd").first()).isNull()
        }

    @Test
    fun `rate limit failure propagates and keeps cache untouched`() =
        runTest {
            marketDao.upsertAll(listOf(cachedEntity("cached", price = "1.00")))
            remote.marketsResult = Result.Failure(AppError.RateLimit)

            val result = repository.refreshMarkets("usd")

            assertThat((result as Result.Failure).error).isEqualTo(AppError.RateLimit)
            assertThat(repository.observeMarkets("usd").first().single().id).isEqualTo("cached")
            // Прошлого обновления нет: meta пишется только при успехе.
            assertThat(repository.observeMarketLastUpdated("usd").first()).isNull()
        }

    @Test
    fun `refreshPriceHistory failure keeps previous points`() =
        runTest {
            historyDao.insertAll(
                listOf(
                    PriceHistoryEntity(
                        coinId = "bitcoin",
                        currency = "usd",
                        range = "HOUR_24",
                        timestampMillis = 1L,
                        price = "99",
                    ),
                ),
            )
            remote.chartResult = Result.Failure(AppError.Network)

            val result = repository.refreshPriceHistory("bitcoin", "usd", ChartRange.HOUR_24)

            assertThat(result).isInstanceOf(Result.Failure::class.java)
            val points = repository.observePriceHistory("bitcoin", "usd", ChartRange.HOUR_24).first()
            assertThat(points.single().price.toPlainString()).isEqualTo("99")
            assertThat(cacheMetaDao.store.value).doesNotContainKey("history:bitcoin:usd:HOUR_24")
        }

    @Test
    fun `refreshCoinDetails caches details and last updated`() =
        runTest {
            remote.detailsResult =
                Result.Success(
                    CoinDetails(
                        id = "bitcoin",
                        symbol = "BTC",
                        name = "Bitcoin",
                        imageUrl = null,
                        description = "The original cryptocurrency.",
                        currentPrice = BigDecimal("64000"),
                        priceChangePercentage24h = -1.5,
                        marketCap = null,
                        marketCapRank = 1,
                        totalVolume = null,
                        high24h = null,
                        low24h = null,
                        circulatingSupply = null,
                    ),
                )

            val result = repository.refreshCoinDetails("bitcoin", "usd")

            assertThat(result).isInstanceOf(Result.Success::class.java)
            val details = repository.observeCoinDetails("bitcoin", "usd").first()
            assertThat(details?.currentPrice?.toPlainString()).isEqualTo("64000")
            assertThat(details?.description).isEqualTo("The original cryptocurrency.")
            assertThat(cacheMetaDao.store.value["details:bitcoin:usd"]?.lastUpdatedMillis).isEqualTo(1_000L)
        }

    @Test
    fun `observeCoin and getCoin expose cached coin`() =
        runTest {
            marketDao.upsertAll(listOf(cachedEntity("bitcoin", price = "64000")))

            val observed = repository.observeCoin("bitcoin", "usd").first()
            val fetched = repository.getCoin("bitcoin", "usd")
            val otherCurrency = repository.getCoin("bitcoin", "eur")

            assertThat(observed?.currentPrice?.toPlainString()).isEqualTo("64000")
            assertThat(fetched?.currentPrice?.toPlainString()).isEqualTo("64000")
            assertThat(otherCurrency).isNull()
        }
}

private fun offlineCoin(
    id: String,
    price: String,
) = Coin(
    id = id,
    symbol = id.uppercase().take(3),
    name = id.replaceFirstChar { it.uppercase() },
    imageUrl = null,
    currentPrice = BigDecimal(price),
    priceChangePercentage24h = 0.0,
    marketCap = null,
    marketCapRank = null,
    totalVolume = null,
    high24h = null,
    low24h = null,
    circulatingSupply = null,
)

private fun cachedEntity(
    coinId: String,
    price: String,
) = MarketCoinEntity(
    coinId = coinId,
    currency = "usd",
    symbol = coinId.uppercase().take(3),
    name = coinId,
    imageUrl = null,
    currentPrice = price,
    changePercent24h = 0.0,
    marketCap = null,
    marketCapRank = null,
    totalVolume = null,
    high24h = null,
    low24h = null,
    circulatingSupply = null,
)

private class OfflineFakeRemote : MarketRemoteDataSource {
    var marketsResult: Result<List<Coin>> = Result.Success(emptyList())
    var chartResult: Result<List<PricePoint>> = Result.Success(emptyList())
    var detailsResult: Result<CoinDetails> =
        Result.Failure(AppError.NotFound)

    override suspend fun getMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ) = marketsResult

    override suspend fun getCoinDetails(
        coinId: String,
        currency: String,
    ): Result<CoinDetails> = detailsResult

    override suspend fun getMarketChart(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = chartResult

    override suspend fun search(query: String): Result<List<Coin>> = Result.Success(emptyList())

    override suspend fun ping(): Result<Long> = Result.Success(0L)
}
