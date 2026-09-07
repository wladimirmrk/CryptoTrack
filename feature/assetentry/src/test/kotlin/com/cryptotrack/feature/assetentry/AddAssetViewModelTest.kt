package com.cryptotrack.feature.assetentry

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
import app.cash.turbine.test
import com.cryptotrack.feature.assetentry.domain.ObserveEntryCoinUseCase
import com.cryptotrack.feature.assetentry.presentation.AddAssetUiState
import com.cryptotrack.feature.assetentry.presentation.AddAssetViewModel
import com.cryptotrack.feature.assetentry.presentation.AmountInputError
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Explicit StandardTestDispatcher as Main (SearchViewModelTest pattern): ViewModel
 * launches queue on the runTest scheduler, so advanceUntilIdle() runs them
 * deterministically. settledState() reads the settled StateFlow via a fresh
 * subscription that emits exactly once from the current source values.
 */
class AddAssetViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private val portfolio = FakePortfolioRepository()
    private val market = FakeMarketRepository()
    private val settings = FakeSettingsRepository()

    // Lazy: must be created after setUp() installs the test Main dispatcher.
    private val viewModel: AddAssetViewModel by lazy {
        AddAssetViewModel(
            savedStateHandle = SavedStateHandle(mapOf("coinId" to "btc")),
            portfolioRepository = portfolio,
            observeEntryCoin = ObserveEntryCoinUseCase(market, settings),
        )
    }

    private suspend fun TestScope.settledState(): AddAssetUiState {
        var state: AddAssetUiState? = null
        viewModel.uiState.test {
            // Value at subscribe time; the upstream may or may not have been hot.
            state = awaitItem()
            advanceUntilIdle() // run the sharing upstream to quiescence
            // The sharing re-emits only when the state actually changed since the
            // last collection; if it did, the newest event is the settled state.
            val remaining = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<AddAssetUiState>>()
            if (remaining.isNotEmpty()) state = remaining.last().value
        }
        return requireNotNull(state)
    }

    @Test
    fun `coin data renders price and 24h change`() = runTest {
        market.coins.value = listOf(coin("btc", "64230", 2.4))

        val state = settledState()
        assertThat(state.isLoading).isFalse()
        assertThat(state.coinName).isEqualTo("btc")
        assertThat(state.coinSymbol).isEqualTo("BTC")
        assertThat(state.priceText).isEqualTo("$64,230.00")
        assertThat(state.changeText).isEqualTo("+2.4%")
        assertThat(state.isPriceUp).isTrue()
    }

    @Test
    fun `coin outside cache keeps form with dash price`() = runTest {
        val state = settledState()
        assertThat(state.isLoading).isFalse()
        assertThat(state.priceText).isNull()
        assertThat(state.estimatedValueText).isNull()
        assertThat(state.coinSymbol).isEqualTo("BTC")
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun `empty amount shows zero estimate and keeps save disabled`() = runTest {
        market.coins.value = listOf(coin("btc", "100", -0.5))

        val state = settledState()
        assertThat(state.amountError).isNull()
        assertThat(state.estimatedValueText).isEqualTo("$0.00")
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun `valid amount enables save with live estimated value`() = runTest {
        market.coins.value = listOf(coin("btc", "64230", 2.4))
        viewModel.onAmountChange("0.25")

        val state = settledState()
        assertThat(state.amountInput).isEqualTo("0.25")
        assertThat(state.amountError).isNull()
        assertThat(state.estimatedValueText).isEqualTo("$16,057.50")
        assertThat(state.isSaveEnabled).isTrue()
    }

    @Test
    fun `zero amount disables save with inline error`() = runTest {
        market.coins.value = listOf(coin("btc", "100", 0.0))
        viewModel.onAmountChange("0")

        val state = settledState()
        assertThat(state.amountError).isEqualTo(AmountInputError.NotPositive)
        assertThat(state.isSaveEnabled).isFalse()
    }

    @Test
    fun `input is sanitized to decimal dot format`() = runTest {
        viewModel.onAmountChange("1,5")

        val state = settledState()
        assertThat(state.amountInput).isEqualTo("1.5")
        assertThat(state.amountError).isNull()
        assertThat(state.isSaveEnabled).isTrue()
    }

    @Test
    fun `save calls saveHolding with parsed amount`() = runTest {
        market.coins.value = listOf(coin("btc", "64230", 2.4))
        viewModel.onAmountChange("0.25")
        advanceUntilIdle()
        assertThat(settledState().isSaveEnabled).isTrue()

        viewModel.onSave()
        advanceUntilIdle()

        assertThat(portfolio.mergedSaves).containsExactly("btc" to BigDecimal("0.25"))
        assertThat(portfolio.holdings.value.single().amount)
            .isEquivalentAccordingToCompareTo(BigDecimal("0.25"))

        val final = settledState()
        assertThat(final.isSaved).isTrue()
        assertThat(final.isSaving).isFalse()
        assertThat(final.isSaveEnabled).isFalse()
    }

    @Test
    fun `save with invalid input does not write`() = runTest {
        viewModel.onAmountChange("0")
        viewModel.onSave()
        advanceUntilIdle()
        assertThat(portfolio.mergedSaves).isEmpty()

        viewModel.onAmountChange("")
        viewModel.onSave()
        advanceUntilIdle()
        assertThat(portfolio.mergedSaves).isEmpty()
    }

    @Test
    fun `repeated save of the same coin merges amounts`() = runTest {
        market.coins.value = listOf(coin("btc", "64230", 2.4))
        viewModel.onAmountChange("0.25")
        viewModel.onSave()
        advanceUntilIdle()
        viewModel.onAmountChange("1")
        viewModel.onSave()
        advanceUntilIdle()

        assertThat(portfolio.mergedSaves).hasSize(2)
        assertThat(portfolio.holdings.value.single().amount)
            .isEquivalentAccordingToCompareTo(BigDecimal("1.25"))
    }
}
