package com.cryptotrack.feature.assetentry.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.format.MoneyFormat
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.feature.assetentry.domain.AssetAmountValidation
import com.cryptotrack.feature.assetentry.domain.AssetAmountValidator
import com.cryptotrack.feature.assetentry.domain.EstimatedValueCalculator
import com.cryptotrack.feature.assetentry.domain.ObserveEntryCoinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddAssetUiState(
    val isLoading: Boolean = false,
    val coinName: String = "",
    val coinSymbol: String = "",
    val coinImageUrl: String? = null,
    val priceText: String? = null,
    val changeText: String? = null,
    val isPriceUp: Boolean = true,
    val amountInput: String = "",
    val amountError: AmountInputError? = null,
    val estimatedValueText: String? = null,
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
)

@HiltViewModel
class AddAssetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val portfolioRepository: PortfolioRepository,
    observeEntryCoin: ObserveEntryCoinUseCase,
) : ViewModel() {

    // Type-safe navigation puts route arguments into SavedStateHandle by property name.
    private val coinId: String = checkNotNull(savedStateHandle["coinId"])

    private val amountInput = MutableStateFlow("")
    private val isSaving = MutableStateFlow(false)
    private val isSaved = MutableStateFlow(false)

    val uiState: StateFlow<AddAssetUiState> = combine(
        observeEntryCoin(coinId),
        amountInput,
        isSaving,
        isSaved,
    ) { coin, input, saving, saved ->
        val validation = AssetAmountValidator.validate(input)
        val estimated = EstimatedValueCalculator.compute(
            estimatedAmount(validation),
            coin?.currentPrice,
        )
        AddAssetUiState(
            isLoading = false,
            coinName = coin?.name.orEmpty(),
            coinSymbol = coin?.symbol ?: coinId.uppercase(Locale.US),
            coinImageUrl = coin?.imageUrl,
            priceText = coin?.currentPrice?.let(MoneyFormat::usd),
            changeText = coin?.let { MoneyFormat.percent(it.priceChangePercentage24h) },
            isPriceUp = (coin?.priceChangePercentage24h ?: 0.0) >= 0.0,
            amountInput = input,
            amountError = amountInputError(validation),
            estimatedValueText = estimated?.let(MoneyFormat::usd),
            isSaveEnabled = validation is AssetAmountValidation.Valid && !saving && !saved,
            isSaving = saving,
            isSaved = saved,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AddAssetUiState(isLoading = true),
    )

    fun onAmountChange(raw: String) {
        amountInput.update { AssetAmountValidator.sanitize(raw) }
    }

    /** Merge semantics (ADR-017): saveHolding sums with an existing holding. */
    fun onSave() {
        val validation = AssetAmountValidator.validate(amountInput.value)
        if (validation !is AssetAmountValidation.Valid) return
        viewModelScope.launch {
            isSaving.update { true }
            portfolioRepository.saveHolding(coinId, validation.amount)
            isSaving.update { false }
            isSaved.update { true }
        }
    }
}
