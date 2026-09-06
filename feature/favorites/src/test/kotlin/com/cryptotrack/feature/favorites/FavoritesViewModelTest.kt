package com.cryptotrack.feature.favorites

import app.cash.turbine.test
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.favorites.presentation.FavoritesViewModel
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

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())
    override suspend fun setCurrency(currency: Currency) = Unit
    override suspend fun setDarkMode(enabled: Boolean) = Unit
    override suspend fun setRefreshInterval(interval: RefreshInterval) = Unit
}

private class FakeFavoritesRepository : FavoritesRepository {
    val favorites = MutableStateFlow<List<Coin>>(emptyList())
    val ids = MutableStateFlow<Set<String>>(emptySet())

    override fun observeFavorites(currency: String): Flow<List<Coin>> = favorites
    override fun observeFavoriteIds(): Flow<Set<String>> = ids
    override fun observeIsFavorite(coinId: String): Flow<Boolean> = ids.map { it.contains(coinId) }
    override suspend fun setFavorite(coinId: String, isFavorite: Boolean) {
        ids.value = if (isFavorite) ids.value + coinId else ids.value - coinId
    }
}

private fun coin(id: String) = Coin(
    id = id, symbol = id.uppercase().take(3), name = id, imageUrl = null,
    currentPrice = BigDecimal("1.00"), priceChangePercentage24h = 1.0,
    marketCap = null, marketCapRank = null, totalVolume = null,
    high24h = null, low24h = null, circulatingSupply = null,
)

class FavoritesViewModelTest {

    private val repository = FakeFavoritesRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `empty favorites show empty state`() = runTest {
        val viewModel = FavoritesViewModel(repository, FakeSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.isEmpty).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorites list exposes count and coins`() = runTest {
        repository.favorites.value = listOf(coin("bitcoin"), coin("ethereum"))
        repository.ids.value = setOf("bitcoin", "ethereum")
        val viewModel = FavoritesViewModel(repository, FakeSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.coins).hasSize(2)
            assertThat(state.countText).isEqualTo("2 Assets")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle removes favorite`() = runTest {
        repository.favorites.value = listOf(coin("bitcoin"))
        repository.ids.value = setOf("bitcoin")
        val viewModel = FavoritesViewModel(repository, FakeSettingsRepository())

        viewModel.onFavoriteToggle("bitcoin")
        advanceUntilIdle()

        assertThat(repository.ids.value).isEmpty()
    }
}
