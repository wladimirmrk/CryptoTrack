package com.cryptotrack.core.data

import com.cryptotrack.core.common.coroutines.DispatchersProvider
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

private class FakeRemote : MarketRemoteDataSource {
    var marketsResult: Result<List<Coin>> = Result.Success(emptyList())
    var chartResult: Result<List<PricePoint>> = Result.Success(emptyList())
    var searchResult: Result<List<Coin>> = Result.Success(emptyList())

    override suspend fun getMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ) = marketsResult

    override suspend fun getCoinDetails(
        coinId: String,
        currency: String,
    ): Result<CoinDetails> = Result.Failure(AppError.NotFound)

    override suspend fun getMarketChart(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = chartResult

    override suspend fun search(query: String) = searchResult

    override suspend fun ping(): Result<Long> = Result.Success(42L)
}

private class FakeDispatchers : DispatchersProvider {
    private val dispatcher = UnconfinedTestDispatcher()
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}

class MarketRepositoryImplTest {
    private val remote = FakeRemote()
    private val marketDao = InMemoryMarketDao()
    private val historyDao = InMemoryPriceHistoryDao()
    private val cacheMetaDao = InMemoryCacheMetaDao()
    private val repository =
        MarketRepositoryImpl(
            remoteDataSource = remote,
            database = TestDatabase(market = marketDao, priceHistory = historyDao, cacheMeta = cacheMetaDao),
            dispatchers = FakeDispatchers(),
            timeProvider = FakeTimeProvider(now = 5_000L),
        )

    @Test
    fun `refreshMarkets writes cache and last updated`() =
        runTest {
            remote.marketsResult =
                Result.Success(
                    listOf(
                        Coin(
                            id = "bitcoin",
                            symbol = "BTC",
                            name = "Bitcoin",
                            imageUrl = null,
                            currentPrice = BigDecimal("45000"),
                            priceChangePercentage24h = 2.4,
                            marketCap = null,
                            marketCapRank = 1,
                            totalVolume = null,
                            high24h = null,
                            low24h = null,
                            circulatingSupply = null,
                        ),
                    ),
                )

            val result = repository.refreshMarkets("usd")

            assert(result is Result.Success)
            val coins = repository.observeMarkets("usd").first()
            assertThat(coins).hasSize(1)
            assertThat(coins.first().currentPrice.toPlainString()).isEqualTo("45000")
            assertThat(cacheMetaDao.store.value["market:usd"]?.lastUpdatedMillis).isEqualTo(5_000L)
        }

    @Test
    fun `refreshMarkets failure keeps cache untouched`() =
        runTest {
            marketDao.upsertAll(
                listOf(
                    MarketCoinEntity(
                        coinId = "cached",
                        currency = "usd",
                        symbol = "OLD",
                        name = "Cached",
                        imageUrl = null,
                        currentPrice = "1.00",
                        changePercent24h = 0.0,
                        marketCap = null,
                        marketCapRank = null,
                        totalVolume = null,
                        high24h = null,
                        low24h = null,
                        circulatingSupply = null,
                    ),
                ),
            )
            remote.marketsResult = Result.Failure(AppError.Network)

            val result = repository.refreshMarkets("usd")

            assertThat((result as Result.Failure).error).isEqualTo(AppError.Network)
            assertThat(repository.observeMarkets("usd").first()).hasSize(1)
        }

    @Test
    fun `searchCoins enriches results with cached prices`() =
        runTest {
            marketDao.upsertAll(
                listOf(
                    MarketCoinEntity(
                        coinId = "bitcoin",
                        currency = "usd",
                        symbol = "BTC",
                        name = "Bitcoin",
                        imageUrl = null,
                        currentPrice = "64230.50",
                        changePercent24h = 2.45,
                        marketCap = null,
                        marketCapRank = null,
                        totalVolume = null,
                        high24h = null,
                        low24h = null,
                        circulatingSupply = null,
                    ),
                ),
            )
            remote.searchResult =
                Result.Success(
                    listOf(
                        Coin(
                            id = "bitcoin",
                            symbol = "BTC",
                            name = "Bitcoin",
                            imageUrl = null,
                            currentPrice = BigDecimal.ZERO,
                            priceChangePercentage24h = 0.0,
                            marketCap = null,
                            marketCapRank = null,
                            totalVolume = null,
                            high24h = null,
                            low24h = null,
                            circulatingSupply = null,
                        ),
                        Coin(
                            id = "bitcoin-cash",
                            symbol = "BCH",
                            name = "Bitcoin Cash",
                            imageUrl = null,
                            currentPrice = BigDecimal.ZERO,
                            priceChangePercentage24h = 0.0,
                            marketCap = null,
                            marketCapRank = null,
                            totalVolume = null,
                            high24h = null,
                            low24h = null,
                            circulatingSupply = null,
                        ),
                    ),
                )

            val coins = (repository.searchCoins("bit", "usd") as Result.Success).data

            assertThat(coins.first { it.id == "bitcoin" }.currentPrice.toPlainString()).isEqualTo("64230.50")
            assertThat(coins.first { it.id == "bitcoin-cash" }.currentPrice.toPlainString()).isEqualTo("0")
        }

    @Test
    fun `refreshPriceHistory replaces range atomically`() =
        runTest {
            historyDao.insertAll(
                listOf(
                    PriceHistoryEntity(
                        coinId = "bitcoin",
                        currency = "usd",
                        range = "DAY_7",
                        timestampMillis = 1L,
                        price = "1",
                    ),
                ),
            )
            remote.chartResult =
                Result.Success(
                    listOf(PricePoint(timestampMillis = 10L, price = BigDecimal("101"))),
                )

            repository.refreshPriceHistory("bitcoin", "usd", ChartRange.DAY_7)

            val points = repository.observePriceHistory("bitcoin", "usd", ChartRange.DAY_7).first()
            assertThat(points).hasSize(1)
            assertThat(points.single().price.toPlainString()).isEqualTo("101")
            assertThat(cacheMetaDao.store.value).containsKey("history:bitcoin:usd:DAY_7")
        }

    @Test
    fun `pingApi returns latency`() =
        runTest {
            assertThat((repository.pingApi() as Result.Success).data).isEqualTo(42L)
        }
}
