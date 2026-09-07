package com.cryptotrack.feature.market

import app.cash.turbine.test
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.market.domain.MarketFilter
import com.cryptotrack.feature.market.domain.MarketSortBy
import com.cryptotrack.feature.market.domain.ObserveMarketUseCase
import com.cryptotrack.feature.market.domain.RefreshMarketUseCase
import com.cryptotrack.feature.market.presentation.MarketViewModel
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Test

private class FakeMarketRepository : MarketRepository {
    val coins = MutableStateFlow<List<Coin>>(emptyList())
    val lastUpdated = MutableStateFlow<Long?>(null)
    var refreshResult: Result<Unit> = Result.Success(Unit)
    var refreshCalls = 0

    override fun observeMarkets(currency: String): Flow<List<Coin>> = coins
    override suspend fun refreshMarkets(currency: String, page: Int, perPage: Int): Result<Unit> {
        refreshCalls++
        return refreshResult.also { if (it is Result.Success) lastUpdated.value = 1_000L }
    }

    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = lastUpdated
    override fun observeCoin(coinId: String, currency: String): Flow<Coin?> =
        coins.map { list -> list.find { it.id == coinId } }

    override suspend fun getCoin(coinId: String, currency: String): Coin? =
        coins.value.find { it.id == coinId }

    override fun observeCoinDetails(coinId: String, currency: String) = throw NotImplementedError()
    override suspend fun refreshCoinDetails(coinId: String, currency: String) = throw NotImplementedError()
    override fun observePriceHistory(coinId: String, currency: String, range: com.cryptotrack.core.model.ChartRange) =
        throw NotImplementedError()

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: com.cryptotrack.core.model.ChartRange,
    ) = throw NotImplementedError()

    override suspend fun searchCoins(query: String, currency: String) = throw NotImplementedError()
    override suspend fun pingApi() = throw NotImplementedError()
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCurrency(currency: Currency) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setDarkMode(enabled: Boolean) = Unit
    override suspend fun setRefreshInterval(interval: RefreshInterval) {
        settings.value = settings.value.copy(refreshInterval = interval)
    }
}

private class FakeFavoritesRepository : FavoritesRepository {
    val ids = MutableStateFlow<Set<String>>(emptySet())
    override fun observeFavorites(currency: String) = throw NotImplementedError()
    override fun observeFavoriteIds(): Flow<Set<String>> = ids
    override fun observeIsFavorite(coinId: String): Flow<Boolean> =
        ids.map { it.contains(coinId) }

    override suspend fun setFavorite(coinId: String, isFavorite: Boolean) {
        ids.value = if (isFavorite) ids.value + coinId else ids.value - coinId
    }
}

private fun coin(id: String, name: String, price: String, cap: Long, change: Double) = Coin(
    id = id, symbol = id.uppercase().take(3), name = name, imageUrl = null,
    currentPrice = BigDecimal(price), priceChangePercentage24h = change,
    marketCap = BigDecimal(cap), marketCapRank = null, totalVolume = null,
    high24h = null, low24h = null, circulatingSupply = null,
)

class MarketViewModelTest {

    private val repository = FakeMarketRepository()
    private val favorites = FakeFavoritesRepository()
    private val settings = FakeSettingsRepository()

    // Main-диспетчер подключается к scheduler теста (setMain) до создания VM:
    // viewModelScope захватывает Main в момент stateIn. Канонический паттерн
    // виртуального времени — как в MarketAutoRefreshTest ниже.
    private fun createViewModel() = MarketViewModel(
        observeMarket = ObserveMarketUseCase(repository, settings),
        refreshMarket = RefreshMarketUseCase(repository, settings),
        favoritesRepository = favorites,
        settingsRepository = settings,
    )

    @Test
    fun `sorts by market cap descending by default`() = try {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = createViewModel()
            repository.coins.value = listOf(
                coin("a", "Alpha", "1.00", cap = 100L, change = 1.0),
                coin("b", "Beta", "2.00", cap = 300L, change = 2.0),
                coin("c", "Gamma", "3.00", cap = 200L, change = 3.0),
            )
            repository.lastUpdated.value = 1_000L
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem() // initialValue stateIn
                runCurrent()
                val state = awaitItem()
                assertThat(state.coins.map { it.coinId }).containsExactly("b", "c", "a").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }
    } finally {
        Dispatchers.resetMain()
    }

    @Test
    fun `applied filter reorders list`() = try {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = createViewModel()
            repository.coins.value = listOf(
                coin("a", "Alpha", "1.00", cap = 100L, change = 1.0),
                coin("b", "Beta", "2.00", cap = 300L, change = 2.0),
            )
            repository.lastUpdated.value = 1_000L
            advanceUntilIdle()

            viewModel.onApplyFilter(
                MarketFilter(
                    sortBy = MarketSortBy.PRICE,
                    order = com.cryptotrack.feature.market.domain.SortOrder.ASCENDING,
                ),
            )
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem()
                runCurrent()
                val state = awaitItem()
                assertThat(state.coins.map { it.coinId }).containsExactly("a", "b").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }
    } finally {
        Dispatchers.resetMain()
    }

    @Test
    fun `refresh failure with cached data shows offline banner`() = try {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = createViewModel()
            repository.coins.value = listOf(coin("a", "Alpha", "1.00", cap = 100L, change = 1.0))
            repository.lastUpdated.value = 1_000L
            repository.refreshResult = Result.Failure(AppError.Network)
            advanceUntilIdle()

            viewModel.onRefresh(force = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem()
                runCurrent()
                val state = awaitItem()
                assertThat(state.isOffline).isTrue()
                assertThat(state.showError).isFalse()
                assertThat(state.error).isEqualTo(AppError.Network)
                cancelAndIgnoreRemainingEvents()
            }
        }
    } finally {
        Dispatchers.resetMain()
    }

    @Test
    fun `favorite toggle persists and reflects in state`() = try {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = createViewModel()
            repository.coins.value = listOf(coin("a", "Alpha", "1.00", cap = 100L, change = 1.0))
            repository.lastUpdated.value = 1_000L
            advanceUntilIdle()

            viewModel.onFavoriteToggle("a", isFavorite = true)
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem()
                runCurrent()
                val state = awaitItem()
                assertThat(state.coins.single().isFavorite).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }
    } finally {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load respects ttl and marks loading state`() = try {
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val viewModel = createViewModel()
            // Кэш пуст, refresh прошёл успешно → не offline, не fullscreen error.
            advanceUntilIdle()

            viewModel.uiState.test {
                awaitItem()
                runCurrent()
                val state = awaitItem()
                assertThat(state.isOffline).isFalse()
                assertThat(state.showError).isFalse()
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(repository.refreshCalls).isAtLeast(1)
        }
    } finally {
        Dispatchers.resetMain()
    }
}

/**
 * A-7: автообновление маркет-данных. Тикер активен только при активной подписке
 * на uiState; таймеры идут на виртуальном времени (общий scheduler с runTest).
 * resetMain — после runTest: на завершающем проходе runTest ещё идут задачи
 * на Main-диспетчере (остановка WhileSubscribed-upstream).
 */
class MarketAutoRefreshTest {

    @Test
    fun `ticker refreshes on interval without duplicating initial load`() {
        try {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val repository = FakeMarketRepository()
                val settings = FakeSettingsRepository()
                val viewModel = MarketViewModel(
                    observeMarket = ObserveMarketUseCase(repository, settings),
                    refreshMarket = RefreshMarketUseCase(repository, settings),
                    favoritesRepository = FakeFavoritesRepository(),
                    settingsRepository = settings,
                )
                advanceUntilIdle() // init refresh; uiState ещё никем не подписан — тикер неактивен
                val initialCalls = repository.refreshCalls
                assertThat(initialCalls).isEqualTo(1)

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    // Subscription keeps the auto-refresh ticker active (WhileSubscribed).
                    viewModel.uiState.collect { }
                }
                runCurrent() // подписка запускает upstream и тикер; onStart-эмиссия без refresh
                assertThat(repository.refreshCalls).isEqualTo(initialCalls)

                advanceTimeBy(RefreshInterval.FIFTEEN_SECONDS.millis)
                runCurrent()
                assertThat(repository.refreshCalls).isEqualTo(initialCalls + 1)

                advanceTimeBy(RefreshInterval.FIFTEEN_SECONDS.millis)
                runCurrent()
                assertThat(repository.refreshCalls).isEqualTo(initialCalls + 2)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `interval change rebuilds the refresh timer`() {
        try {
            runTest {
                Dispatchers.setMain(StandardTestDispatcher(testScheduler))
                val repository = FakeMarketRepository()
                val settings = FakeSettingsRepository()
                val viewModel = MarketViewModel(
                    observeMarket = ObserveMarketUseCase(repository, settings),
                    refreshMarket = RefreshMarketUseCase(repository, settings),
                    favoritesRepository = FakeFavoritesRepository(),
                    settingsRepository = settings,
                )
                advanceUntilIdle()
                val initialCalls = repository.refreshCalls

                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    viewModel.uiState.collect { }
                }
                advanceTimeBy(RefreshInterval.FIFTEEN_SECONDS.millis)
                runCurrent()
                assertThat(repository.refreshCalls).isEqualTo(initialCalls + 1)

                settings.setRefreshInterval(RefreshInterval.ONE_MINUTE)
                runCurrent() // flatMapLatest перестраивает таймер: следующий тик через 1m

                advanceTimeBy(RefreshInterval.FIFTEEN_SECONDS.millis)
                runCurrent()
                assertThat(repository.refreshCalls).isEqualTo(initialCalls + 1)

                advanceTimeBy(RefreshInterval.ONE_MINUTE.millis)
                runCurrent()
                assertThat(repository.refreshCalls).isEqualTo(initialCalls + 2)
            }
        } finally {
            Dispatchers.resetMain()
        }
    }
}
