package com.cryptotrack.feature.coindetails

import app.cash.turbine.test
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.coindetails.domain.ObserveCoinDetailsUseCase
import com.cryptotrack.feature.coindetails.domain.ObservePriceHistoryUseCase
import com.cryptotrack.feature.coindetails.domain.RefreshCoinDataUseCase
import com.cryptotrack.feature.coindetails.presentation.CoinDetailsViewModel
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeMarketRepository : MarketRepository {
    val detailsFlow = MutableStateFlow<CoinDetails?>(null)
    val history = MutableStateFlow<List<PricePoint>>(emptyList())
    val lastUpdated = MutableStateFlow<Long?>(null)
    var refreshResult: Result<Unit> = Result.Success(Unit)
    var historyCalls = 0
    var lastHistoryRange: ChartRange? = null

    override fun observeMarkets(currency: String): Flow<List<Coin>> = MutableStateFlow(emptyList())
    override suspend fun refreshMarkets(currency: String, page: Int, perPage: Int) = Result.Success(Unit)
    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = lastUpdated
    override fun observeCoin(coinId: String, currency: String): Flow<Coin?> = MutableStateFlow(null)
    override suspend fun getCoin(coinId: String, currency: String): Coin? = null
    override fun observeCoinDetails(coinId: String, currency: String): Flow<CoinDetails?> = detailsFlow

    override suspend fun refreshCoinDetails(coinId: String, currency: String): Result<Unit> {
        lastUpdated.value = 1_000L
        return refreshResult
    }

    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Flow<List<PricePoint>> = history

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Result<Unit> {
        historyCalls++
        lastHistoryRange = range
        return refreshResult
    }

    override suspend fun searchCoins(query: String, currency: String) = throw NotImplementedError()
    override suspend fun pingApi() = throw NotImplementedError()
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCurrency(currency: Currency) = Unit
    override suspend fun setDarkMode(enabled: Boolean) = Unit
    override suspend fun setRefreshInterval(interval: RefreshInterval) = Unit
}

private class FakeFavoritesRepository : FavoritesRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    override fun observeFavorites(currency: String) = throw NotImplementedError()
    override fun observeFavoriteIds(): Flow<Set<String>> = ids
    override fun observeIsFavorite(coinId: String): Flow<Boolean> = ids.map { it.contains(coinId) }
    override suspend fun setFavorite(coinId: String, isFavorite: Boolean) {
        ids.value = if (isFavorite) ids.value + coinId else ids.value - coinId
    }
}

private fun details() = CoinDetails(
    id = "bitcoin", symbol = "BTC", name = "Bitcoin", imageUrl = null,
    description = "Decentralized cryptocurrency.",
    currentPrice = BigDecimal("64230.50"), priceChangePercentage24h = 2.45,
    marketCap = BigDecimal(1.24e12), marketCapRank = 1, totalVolume = null,
    high24h = null, low24h = null, circulatingSupply = BigDecimal(19_500_000.0),
)

class CoinDetailsViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val repository = FakeMarketRepository()
    private val favorites = FakeFavoritesRepository()
    private val viewModel = CoinDetailsViewModel(
        savedStateHandle = androidx.lifecycle.SavedStateHandle(mapOf("coinId" to "bitcoin")),
        observeDetails = ObserveCoinDetailsUseCase(repository, FakeSettingsRepository()),
        observeHistory = ObservePriceHistoryUseCase(repository, FakeSettingsRepository()),
        refreshCoinData = RefreshCoinDataUseCase(repository, FakeSettingsRepository()),
        favoritesRepository = favorites,
    )

    @Test
    fun `details render from cache with formatted values`() = runTest {
        repository.detailsFlow.value = details()
        repository.lastUpdated.value = 1_000L
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.name).isEqualTo("Bitcoin")
            assertThat(state.priceText).isEqualTo("$64,230.50")
            assertThat(state.marketCapRank).isEqualTo(1)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `range switch refreshes history for selected range`() = runTest {
        repository.detailsFlow.value = details()
        repository.lastUpdated.value = 1_000L
        repository.history.value = listOf(
            PricePoint(1L, BigDecimal("100")),
            PricePoint(2L, BigDecimal("110")),
        )
        advanceUntilIdle()

        viewModel.onRangeSelect(ChartRange.YEAR_1)
        advanceUntilIdle()

        assertThat(repository.lastHistoryRange).isEqualTo(ChartRange.YEAR_1)
        assertThat(repository.historyCalls).isAtLeast(2)
    }

    @Test
    fun `favorite toggle persists`() = runTest {
        repository.detailsFlow.value = details()
        repository.lastUpdated.value = 1_000L
        advanceUntilIdle()

        viewModel.onFavoriteToggle()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isFavorite).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh failure with cached details shows offline banner`() = runTest {
        repository.detailsFlow.value = details()
        repository.lastUpdated.value = 1_000L
        repository.refreshResult = Result.Failure(AppError.Network)
        advanceUntilIdle()

        viewModel.onRefresh()
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isOffline).isTrue()
            assertThat(state.showError).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
