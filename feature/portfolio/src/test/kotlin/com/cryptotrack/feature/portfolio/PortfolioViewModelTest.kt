package com.cryptotrack.feature.portfolio

import app.cash.turbine.test
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.Holding
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.portfolio.domain.ObservePortfolioUseCase
import com.cryptotrack.feature.portfolio.presentation.PortfolioViewModel
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakePortfolioRepository : PortfolioRepository {
    val holdings = MutableStateFlow<List<Holding>>(emptyList())

    override fun observeHoldings(): Flow<List<Holding>> = holdings

    override fun observeHolding(coinId: String): Flow<Holding?> =
        holdings.map { list -> list.find { it.coinId == coinId } }

    override suspend fun saveHolding(
        coinId: String,
        amount: BigDecimal,
    ) = throw NotImplementedError()

    override suspend fun setHoldingAmount(
        coinId: String,
        amount: BigDecimal,
    ) = throw NotImplementedError()

    override suspend fun removeHolding(coinId: String) = throw NotImplementedError()
}

private class FakeMarketRepository : MarketRepository {
    val coins = MutableStateFlow<List<Coin>>(emptyList())
    val lastUpdated = MutableStateFlow<Long?>(null)
    var refreshResult: Result<Unit> = Result.Success(Unit)
    var refreshCalls = 0

    override fun observeMarkets(currency: String): Flow<List<Coin>> = coins
    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = lastUpdated

    override suspend fun refreshMarkets(
        currency: String,
        page: Int,
        perPage: Int,
    ): Result<Unit> {
        refreshCalls++
        return refreshResult.also { if (it is Result.Success) lastUpdated.value = 1_000L }
    }

    override fun observeCoin(
        coinId: String,
        currency: String,
    ): Flow<Coin?> = coins.map { list -> list.find { it.id == coinId } }

    override suspend fun getCoin(
        coinId: String,
        currency: String,
    ): Coin? = coins.value.find { it.id == coinId }

    override fun observeCoinDetails(
        coinId: String,
        currency: String,
    ) = throw NotImplementedError()

    override suspend fun refreshCoinDetails(
        coinId: String,
        currency: String,
    ) = throw NotImplementedError()

    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = throw NotImplementedError()

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = throw NotImplementedError()

    override suspend fun searchCoins(
        query: String,
        currency: String,
    ) = throw NotImplementedError()

    override suspend fun pingApi() = throw NotImplementedError()
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCurrency(currency: Currency) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setDarkMode(enabled: Boolean) = Unit
    override suspend fun setRefreshInterval(interval: RefreshInterval) = Unit
}

private fun coin(id: String, price: String, change: Double) = Coin(
    id = id, symbol = id.uppercase(), name = id, imageUrl = null,
    currentPrice = BigDecimal(price), priceChangePercentage24h = change,
    marketCap = null, marketCapRank = null, totalVolume = null,
    high24h = null, low24h = null, circulatingSupply = null,
)

private fun holding(coinId: String, amount: String) =
    Holding(coinId = coinId, amount = BigDecimal(amount), updatedAtMillis = 0L)

class PortfolioViewModelTest {

    private val portfolio = FakePortfolioRepository()
    private val market = FakeMarketRepository()
    private val settings = FakeSettingsRepository()
    private val viewModel = PortfolioViewModel(
        observePortfolio = ObservePortfolioUseCase(portfolio, market, settings),
        marketRepository = market,
        settingsRepository = settings,
    )

    @Test
    fun `empty holdings show empty state after first emission`() = runTest {
        advanceUntilIdle() // init onRefresh

        viewModel.uiState.test {
            awaitItem() // initialValue stateIn
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isEmpty).isTrue()
            assertThat(state.isLoading).isFalse()
            assertThat(state.showError).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(market.refreshCalls).isAtLeast(1)
    }

    @Test
    fun `holdings and prices compute aggregates`() = runTest {
        // btc 0.15 × $1000 = $150; eth 1.0 × $100 = $100; both +10% → total $250, +$25 (+11.1%).
        portfolio.holdings.value = listOf(holding("btc", "0.15"), holding("eth", "1.0"))
        market.coins.value = listOf(coin("btc", "1000", 10.0), coin("eth", "100", 10.0))
        market.lastUpdated.value = 1_000L
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.totalBalanceText).isEqualTo("$250.00")
            assertThat(state.change24hText).isEqualTo("$25.00 (+11.1%)")
            assertThat(state.isUp).isTrue()
            assertThat(state.allocations.map { it.coinId }).containsExactly("btc", "eth").inOrder()
            assertThat(state.allocations.first().symbol).isEqualTo("BTC")
            assertThat(state.allocations.first().percent).isWithin(0.01).of(60.0)
            assertThat(state.positions).hasSize(2)
            val btc = state.positions.first()
            assertThat(btc.coinId).isEqualTo("btc")
            assertThat(btc.symbol).isEqualTo("BTC")
            assertThat(btc.amountText).isEqualTo("0.15 BTC")
            assertThat(btc.valueText).isEqualTo("$150.00")
            assertThat(btc.changeText).isEqualTo("+10.0%")
            assertThat(btc.isUp).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure with positions shows offline banner`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "1"))
        market.coins.value = listOf(coin("btc", "100", 0.0))
        market.lastUpdated.value = 1_000L
        market.refreshResult = Result.Failure(AppError.Network)
        advanceUntilIdle()
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isOffline).isTrue()
            assertThat(state.showError).isFalse()
            assertThat(state.error).isEqualTo(AppError.Network)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure with empty positions shows error state`() = runTest {
        market.refreshResult = Result.Failure(AppError.Network)
        advanceUntilIdle()
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.showError).isTrue()
            assertThat(state.isOffline).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `successful refresh clears error`() = runTest {
        market.refreshResult = Result.Failure(AppError.Network)
        advanceUntilIdle()
        market.refreshResult = Result.Success(Unit)
        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.error).isNull()
            assertThat(state.showError).isFalse()
            assertThat(state.isEmpty).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
