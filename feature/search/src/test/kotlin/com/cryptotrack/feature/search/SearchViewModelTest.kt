package com.cryptotrack.feature.search

import app.cash.turbine.test
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.search.domain.SearchCoinsUseCase
import com.cryptotrack.feature.search.presentation.SearchViewModel
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

private class FakeMarketRepository : MarketRepository {
    var searchResult: Result<List<Coin>> = Result.Success(emptyList())
    var lastQuery: String? = null

    override fun observeMarkets(currency: String): Flow<List<Coin>> = MutableStateFlow(emptyList())
    override suspend fun refreshMarkets(currency: String, page: Int, perPage: Int) = Result.Success(Unit)
    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = MutableStateFlow(null)
    override fun observeCoin(coinId: String, currency: String): Flow<Coin?> = MutableStateFlow(null)
    override suspend fun getCoin(coinId: String, currency: String): Coin? = null
    override fun observeCoinDetails(coinId: String, currency: String) = throw NotImplementedError()
    override suspend fun refreshCoinDetails(coinId: String, currency: String) = throw NotImplementedError()
    override fun observePriceHistory(coinId: String, currency: String, range: ChartRange) = throw NotImplementedError()
    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = throw NotImplementedError()

    override suspend fun searchCoins(query: String, currency: String): Result<List<Coin>> {
        lastQuery = query
        return searchResult
    }

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

private fun coin(id: String) = Coin(
    id = id, symbol = "BTC", name = "Bitcoin", imageUrl = null,
    currentPrice = BigDecimal("1.00"), priceChangePercentage24h = 1.0,
    marketCap = null, marketCapRank = null, totalVolume = null,
    high24h = null, low24h = null, circulatingSupply = null,
)

class SearchViewModelTest {

    private val repository = FakeMarketRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `query debounce triggers search and exposes results`() = runTest {
        repository.searchResult = Result.Success(listOf(coin("bitcoin")))
        val viewModel = SearchViewModel(
            searchCoins = SearchCoinsUseCase(repository, FakeSettingsRepository()),
            favoritesRepository = FakeFavoritesRepository(),
        )

        viewModel.uiState.test {
            awaitItem()
            viewModel.onQueryChange("bit")
            advanceTimeBy(SearchCoinsUseCase.DEBOUNCE_MILLIS + 10)
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.results).hasSize(1)
            assertThat(repository.lastQuery).isEqualTo("bit")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `blank query keeps results empty`() = runTest {
        repository.searchResult = Result.Success(listOf(coin("bitcoin")))
        val viewModel = SearchViewModel(
            searchCoins = SearchCoinsUseCase(repository, FakeSettingsRepository()),
            favoritesRepository = FakeFavoritesRepository(),
        )

        viewModel.onQueryChange("   ")
        advanceTimeBy(SearchCoinsUseCase.DEBOUNCE_MILLIS + 10)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.results).isEmpty()
            assertThat(state.hasQuery).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `search error surfaces nothing and keeps empty results`() = runTest {
        repository.searchResult = Result.Failure(AppError.Network)
        val viewModel = SearchViewModel(
            searchCoins = SearchCoinsUseCase(repository, FakeSettingsRepository()),
            favoritesRepository = FakeFavoritesRepository(),
        )

        viewModel.onQueryChange("bit")
        advanceTimeBy(SearchCoinsUseCase.DEBOUNCE_MILLIS + 10)
        advanceUntilIdle()

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.results).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
