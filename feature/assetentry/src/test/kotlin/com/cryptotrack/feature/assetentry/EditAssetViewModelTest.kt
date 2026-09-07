package com.cryptotrack.feature.assetentry

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.Event
import app.cash.turbine.test
import com.cryptotrack.feature.assetentry.domain.ObserveEntryCoinUseCase
import com.cryptotrack.feature.assetentry.presentation.EditAssetUiState
import com.cryptotrack.feature.assetentry.presentation.EditAssetViewModel
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
class EditAssetViewModelTest {

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

    private fun viewModel() = EditAssetViewModel(
        savedStateHandle = SavedStateHandle(mapOf("coinId" to "btc")),
        portfolioRepository = portfolio,
        observeEntryCoin = ObserveEntryCoinUseCase(market, settings),
    )

    private suspend fun TestScope.settledState(viewModel: EditAssetViewModel): EditAssetUiState {
        var state: EditAssetUiState? = null
        viewModel.uiState.test {
            // Value at subscribe time; the upstream may or may not have been hot.
            state = awaitItem()
            advanceUntilIdle() // run the sharing upstream to quiescence
            // The sharing re-emits only when the state actually changed since the
            // last collection; if it did, the newest event is the settled state.
            val remaining = cancelAndConsumeRemainingEvents()
                .filterIsInstance<Event.Item<EditAssetUiState>>()
            if (remaining.isNotEmpty()) state = remaining.last().value
        }
        return requireNotNull(state)
    }

    @Test
    fun `loads holding amount with live value preview`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "0.15"))
        market.coins.value = listOf(coin("btc", "64230", 2.4))
        val viewModel = viewModel()
        advanceUntilIdle() // prefill

        val state = settledState(viewModel)
        assertThat(state.isLoading).isFalse()
        assertThat(state.isNotFound).isFalse()
        assertThat(state.amountInput).isEqualTo("0.15")
        assertThat(state.coinSymbol).isEqualTo("BTC")
        assertThat(state.currentPriceText).isEqualTo("$64,230.00")
        assertThat(state.estimatedValueText).isEqualTo("$9,634.50")
        assertThat(state.isSaveEnabled).isTrue()
    }

    @Test
    fun `missing holding shows not found state`() = runTest {
        val viewModel = viewModel()

        val state = settledState(viewModel)
        assertThat(state.isNotFound).isTrue()
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `save changes calls setHoldingAmount with parsed amount`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "0.15"))
        val viewModel = viewModel()
        advanceUntilIdle() // prefill

        viewModel.onAmountChange("0.5")
        viewModel.onSave()
        advanceUntilIdle()

        assertThat(portfolio.exactSets).containsExactly("btc" to BigDecimal("0.5"))
        assertThat(portfolio.mergedSaves).isEmpty()
        assertThat(portfolio.holdings.value.single().amount)
            .isEquivalentAccordingToCompareTo(BigDecimal("0.5"))

        val final = settledState(viewModel)
        assertThat(final.isSaved).isTrue()
        assertThat(final.isSaving).isFalse()
    }

    @Test
    fun `remove asset happens only after confirmation`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "0.15"))
        val viewModel = viewModel()
        advanceUntilIdle() // prefill

        viewModel.onDeleteClick()
        advanceUntilIdle()
        assertThat(settledState(viewModel).showDeleteConfirmation).isTrue()
        assertThat(portfolio.removals).isEmpty()

        viewModel.onDismissDelete()
        advanceUntilIdle()
        assertThat(settledState(viewModel).showDeleteConfirmation).isFalse()
        assertThat(portfolio.removals).isEmpty()

        viewModel.onDeleteClick()
        viewModel.onConfirmDelete()
        advanceUntilIdle()
        assertThat(settledState(viewModel).isRemoved).isTrue()
        assertThat(portfolio.removals).containsExactly("btc")
        assertThat(portfolio.holdings.value).isEmpty()
    }

    @Test
    fun `confirmed remove exposes removed state`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "0.15"))
        val viewModel = viewModel()
        advanceUntilIdle() // prefill

        viewModel.onDeleteClick()
        viewModel.onConfirmDelete()
        advanceUntilIdle()

        assertThat(portfolio.removals).containsExactly("btc")
        val state = settledState(viewModel)
        assertThat(state.isRemoved).isTrue()
        assertThat(state.isDeleting).isFalse()
    }

    @Test
    fun `zero amount disables save with inline error`() = runTest {
        portfolio.holdings.value = listOf(holding("btc", "0.15"))
        val viewModel = viewModel()
        advanceUntilIdle() // prefill

        viewModel.onAmountChange("0")

        val state = settledState(viewModel)
        assertThat(state.amountError).isEqualTo(AmountInputError.NotPositive)
        assertThat(state.isSaveEnabled).isFalse()
    }
}
