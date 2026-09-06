package com.cryptotrack.core.network

import com.cryptotrack.core.common.time.TimeProvider
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.network.dto.CoinDetailsDto
import com.cryptotrack.core.network.dto.CoinImageDto
import com.cryptotrack.core.network.dto.CoinMarketDataDto
import com.cryptotrack.core.network.dto.MarketChartDto
import com.cryptotrack.core.network.dto.MarketCoinDto
import com.cryptotrack.core.network.source.CoinGeckoRemoteDataSource
import com.cryptotrack.core.network.mapper.toCoinDetails
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.math.BigDecimal

private class FakeTimeProvider(var now: Long = 0L) : TimeProvider {
    override fun nowMillis(): Long = now
}

private class FakeApi : CoinGeckoApi {
    var marketChart = MarketChartDto()
    var markets: List<MarketCoinDto> = emptyList()

    override suspend fun getMarkets(
        currency: String,
        order: String,
        perPage: Int,
        page: Int,
        priceChangePercentage: String,
        sparkline: Boolean,
    ): List<MarketCoinDto> = markets

    override suspend fun getCoinDetails(
        coinId: String,
        localization: String,
        tickers: Boolean,
        marketData: Boolean,
        communityData: Boolean,
        developerData: Boolean,
    ): CoinDetailsDto = throw NotImplementedError()

    override suspend fun getMarketChart(coinId: String, currency: String, days: String): MarketChartDto =
        marketChart

    override suspend fun search(query: String) = throw NotImplementedError()

    override suspend fun ping() = throw NotImplementedError()
}

class CoinGeckoRemoteDataSourceTest {

    private val api = FakeApi()
    private val time = FakeTimeProvider(now = 3_600_000L)
    private val dataSource = CoinGeckoRemoteDataSource(api, time)

    @Test
    fun `markets map to domain coins with uppercase symbol`() = runTest {
        api.markets = listOf(
            MarketCoinDto(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                image = "https://x/btc.png",
                current_price = 45230.12,
                priceChangePercentage24h = 2.4,
                market_cap = 1.24e12,
                market_cap_rank = 1,
            ),
        )

        val result = dataSource.getMarkets(currency = "usd", page = 1, perPage = 50)

        val coin = (result.getOrNull() ?: error("expected success")).single()
        assertThat(coin.symbol).isEqualTo("BTC")
        assertThat(coin.currentPrice).isEqualTo(BigDecimal.valueOf(45230.12))
        assertThat(coin.marketCapRank).isEqualTo(1)
    }

    @Test
    fun `hour1 range trims daily data to last hour`() = runTest {
        api.marketChart = MarketChartDto(
            prices = listOf(
                listOf(-7_200_000.0, 100.0),           // 2 часа назад — отрезается
                listOf(600_000.0, 110.0),              // в пределах часа — остаётся
                listOf(3_600_000.0, 120.0),            // сейчас — остаётся
            ),
        )

        val result = dataSource.getMarketChart("bitcoin", "usd", ChartRange.HOUR_1)

        val points = result.getOrNull() ?: error("expected success")
        assertThat(points.map { it.price.toLong() }).containsExactly(110L, 120L).inOrder()
    }

    @Test
    fun `day7 range returns all points`() = runTest {
        api.marketChart = MarketChartDto(prices = listOf(listOf(0.0, 100.0), listOf(1000.0, 101.0)))

        val result = dataSource.getMarketChart("bitcoin", "usd", ChartRange.DAY_7)

        assertThat(result.getOrNull()?.size).isEqualTo(2)
    }

    @Test
    fun `details map currency prices`() {
        val dto = CoinDetailsDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = CoinImageDto(large = "https://x/large.png"),
            market_data = CoinMarketDataDto(
                current_price = mapOf("usd" to 64230.50),
                market_cap = mapOf("usd" to 1.24e12),
                marketCapRank = 1,
                priceChangePercentage24h = 2.45,
            ),
        )

        val details = dto.toCoinDetails("usd")

        assertThat(details.currentPrice).isEqualTo(BigDecimal.valueOf(64230.50))
        assertThat(details.symbol).isEqualTo("BTC")
        assertThat(details.imageUrl).isEqualTo("https://x/large.png")
    }

    @Test
    fun `details without market data fall back to zero price`() {
        val details = CoinDetailsDto(id = "bitcoin").toCoinDetails("usd")

        assertThat(details.currentPrice).isEqualTo(BigDecimal.ZERO)
    }
}
