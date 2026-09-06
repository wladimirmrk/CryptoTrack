package com.cryptotrack.feature.portfolio

import com.cryptotrack.core.model.Allocation
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Holding
import com.cryptotrack.core.model.PortfolioSummary
import com.cryptotrack.feature.portfolio.domain.PortfolioCalculator
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import org.junit.Test

class PortfolioCalculatorTest {

    private fun coin(id: String, price: String, change: Double) = Coin(
        id = id, symbol = id.uppercase(), name = id, imageUrl = null,
        currentPrice = BigDecimal(price), priceChangePercentage24h = change,
        marketCap = null, marketCapRank = null, totalVolume = null,
        high24h = null, low24h = null, circulatingSupply = null,
    )

    private fun holding(coinId: String, amount: String) =
        Holding(coinId = coinId, amount = BigDecimal(amount), updatedAtMillis = 0L)

    @Test
    fun `computes total balance as sum of values`() {
        val result = PortfolioCalculator.compute(
            holdings = listOf(holding("btc", "0.15"), holding("eth", "1.2")),
            coins = listOf(coin("btc", "1000", 0.0), coin("eth", "100", 0.0)),
        )

        assertThat(result.summary.totalBalanceUsd.toDouble()).isWithin(0.01).of(270.0)
    }

    @Test
    fun `computes 24h change from percentage`() {
        // Today: 150 + 100 = 250; +25 absolute → yesterday 225 → 25/225 ≈ 11.1%
        // (mock Portfolio Overview shows 24h % against yesterday's value).
        val result = PortfolioCalculator.compute(
            holdings = listOf(holding("btc", "0.15"), holding("eth", "1.0")),
            coins = listOf(coin("btc", "1000", 10.0), coin("eth", "100", 10.0)),
        )

        val summary = result.summary
        assertThat(summary.change24hUsd.toDouble()).isWithin(0.01).of(25.0)
        assertThat(summary.change24hPercent).isWithin(0.01).of(11.11)
    }

    @Test
    fun `allocation percentages sum to 100`() {
        val result = PortfolioCalculator.compute(
            holdings = listOf(holding("btc", "1"), holding("eth", "1"), holding("sol", "2")),
            coins = listOf(coin("btc", "100", 0.0), coin("eth", "100", 0.0), coin("sol", "100", 0.0)),
        )

        val sum = result.summary.allocations.sumOf { it.percent }
        assertThat(sum).isWithin(0.01).of(100.0)
    }

    @Test
    fun `holdings without cached coin are skipped`() {
        val result = PortfolioCalculator.compute(
            holdings = listOf(holding("btc", "1"), holding("unknown", "5")),
            coins = listOf(coin("btc", "100", 0.0)),
        )

        assertThat(result.positions).hasSize(1)
        assertThat(result.summary.totalBalanceUsd.toPlainString()).isEqualTo("100")
    }

    @Test
    fun `empty portfolio yields zero summary`() {
        val result = PortfolioCalculator.compute(
            holdings = emptyList(),
            coins = listOf(coin("btc", "100", 0.0)),
        )

        assertThat(result.summary.totalBalanceUsd.signum()).isEqualTo(0)
        assertThat(result.summary.change24hPercent).isEqualTo(0.0)
        assertThat(result.summary.allocations).isEmpty()
    }

    @Test
    fun `allocation symbols match positions`() {
        val result = PortfolioCalculator.compute(
            holdings = listOf(holding("btc", "1")),
            coins = listOf(coin("btc", "100", 0.0)),
        )

        val first: Allocation = result.summary.allocations.single()
        assertThat(first.symbol).isEqualTo("BTC")
        assertThat(first.percent).isWithin(0.01).of(100.0)
        val summary: PortfolioSummary = result.summary
        assertThat(summary.allocations).hasSize(1)
    }
}
