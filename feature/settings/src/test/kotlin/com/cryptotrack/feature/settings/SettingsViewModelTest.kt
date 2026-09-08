package com.cryptotrack.feature.settings

import app.cash.turbine.test
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.AppSettings
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.CoinDetails
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.PricePoint
import com.cryptotrack.core.model.RefreshInterval
import com.cryptotrack.feature.settings.presentation.ApiStatus
import com.cryptotrack.feature.settings.presentation.SettingsViewModel
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(AppSettings())

    override suspend fun setCurrency(currency: Currency) {
        settings.value = settings.value.copy(currency = currency)
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        settings.value = settings.value.copy(isDarkMode = enabled)
    }

    override suspend fun setRefreshInterval(interval: RefreshInterval) {
        settings.value = settings.value.copy(refreshInterval = interval)
    }
}

private class FakeMarketRepository : MarketRepository {
    var pingResult: Result<Long> = Result.Success(42L)

    override fun observeMarkets(currency: String): Flow<List<Coin>> = MutableStateFlow(emptyList())
    override suspend fun refreshMarkets(currency: String, page: Int, perPage: Int) = Result.Success(Unit)
    override fun observeMarketLastUpdated(currency: String): Flow<Long?> = MutableStateFlow(null)
    override fun observeCoin(coinId: String, currency: String): Flow<Coin?> = MutableStateFlow(null)
    override suspend fun getCoin(coinId: String, currency: String): Coin? = null
    override fun observeCoinDetails(coinId: String, currency: String): Flow<CoinDetails?> =
        MutableStateFlow(null)

    override suspend fun refreshCoinDetails(coinId: String, currency: String) = Result.Success(Unit)
    override fun observePriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ): Flow<List<PricePoint>> = MutableStateFlow(emptyList())

    override suspend fun refreshPriceHistory(
        coinId: String,
        currency: String,
        range: ChartRange,
    ) = Result.Success(Unit)

    override suspend fun searchCoins(query: String, currency: String) = throw NotImplementedError()
    override suspend fun pingApi(): Result<Long> = pingResult
}

class SettingsViewModelTest {

    private val settings = FakeSettingsRepository()
    private val market = FakeMarketRepository()
    private val viewModel = SettingsViewModel(
        settingsRepository = settings,
        marketRepository = market,
    )

    @Test
    fun `settings values emit into ui state`() = runTest {
        settings.settings.value = AppSettings(
            currency = Currency.EUR,
            isDarkMode = true,
            refreshInterval = RefreshInterval.ONE_MINUTE,
        )

        viewModel.uiState.test {
            awaitItem() // initialValue stateIn
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.currency).isEqualTo(Currency.EUR)
            assertThat(state.isDarkMode).isTrue()
            assertThat(state.refreshInterval).isEqualTo(RefreshInterval.ONE_MINUTE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCurrencySelect updates settings repository`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initialValue stateIn
            advanceUntilIdle()
            awaitItem() // first combined emission with default settings
            viewModel.onCurrencySelect(Currency.EUR)
            val state = awaitItem()
            assertThat(state.currency).isEqualTo(Currency.EUR)
            assertThat(settings.settings.value.currency).isEqualTo(Currency.EUR)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDarkModeToggle updates settings repository`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initialValue stateIn
            advanceUntilIdle()
            awaitItem() // first combined emission with default settings
            viewModel.onDarkModeToggle(true)
            val state = awaitItem()
            assertThat(state.isDarkMode).isTrue()
            assertThat(settings.settings.value.isDarkMode).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefreshIntervalSelect updates settings repository`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initialValue stateIn
            advanceUntilIdle()
            awaitItem() // first combined emission with default settings
            viewModel.onRefreshIntervalSelect(RefreshInterval.ONE_MINUTE)
            val state = awaitItem()
            assertThat(state.refreshInterval).isEqualTo(RefreshInterval.ONE_MINUTE)
            assertThat(settings.settings.value.refreshInterval).isEqualTo(RefreshInterval.ONE_MINUTE)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `api status success reports latency`() = runTest {
        market.pingResult = Result.Success(42L)

        viewModel.uiState.test {
            awaitItem() // initialValue stateIn (Checking)
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.apiStatus).isEqualTo(ApiStatus.Connected(42L))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `api status failure reports unavailable`() = runTest {
        market.pingResult = Result.Failure(AppError.Network)

        viewModel.uiState.test {
            awaitItem() // initialValue stateIn (Checking)
            advanceUntilIdle()
            val state = awaitItem()
            assertThat(state.apiStatus).isEqualTo(ApiStatus.Unavailable)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
