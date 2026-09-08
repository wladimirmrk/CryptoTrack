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
import java.math.BigDecimal
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EditAssetUiState(
    val isLoading: Boolean = false,
    val isNotFound: Boolean = false,
    val coinName: String = "",
    val coinSymbol: String = "",
    val coinImageUrl: String? = null,
    val currentPriceText: String? = null,
    val changeText: String? = null,
    val isPriceUp: Boolean = true,
    val amountInput: String = "",
    val amountError: AmountInputError? = null,
    val estimatedValueText: String? = null,
    val isSaveEnabled: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val isDeleting: Boolean = false,
    val isRemoved: Boolean = false,
)

private data class EditStatus(
    val isLoaded: Boolean,
    val isNotFound: Boolean,
    val isSaving: Boolean,
    val isSaved: Boolean,
)

@HiltViewModel
class EditAssetViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val portfolioRepository: PortfolioRepository,
    observeEntryCoin: ObserveEntryCoinUseCase,
) : ViewModel() {

    // Type-safe navigation puts route arguments into SavedStateHandle by property name.
    private val coinId: String = checkNotNull(savedStateHandle["coinId"])

    private val amountInput = MutableStateFlow("")
    private val isLoaded = MutableStateFlow(false)
    private val isNotFound = MutableStateFlow(false)
    private val isSaving = MutableStateFlow(false)
    private val isSaved = MutableStateFlow(false)
    private val showDeleteConfirmation = MutableStateFlow(false)
    private val isDeleting = MutableStateFlow(false)
    private val isRemoved = MutableStateFlow(false)

    init {
        // Prefill the field from the stored holding once; later holding changes
        // must not overwrite what the user is typing.
        viewModelScope.launch {
            val holding = portfolioRepository.observeHolding(coinId).first()
            if (holding == null) {
                isNotFound.update { true }
            } else {
                amountInput.update { holding.amount.toPlainString() }
                isLoaded.update { true }
            }
        }
    }

    private val dataFlow = combine(
        observeEntryCoin(coinId),
        amountInput,
    ) { coin, input -> coin to input }

    private val statusFlow = combine(
        isLoaded,
        isNotFound,
        isSaving,
        isSaved,
    ) { loaded, notFound, saving, saved ->
        EditStatus(loaded, notFound, saving, saved)
    }

    val uiState: StateFlow<EditAssetUiState> = combine(
        dataFlow,
        statusFlow,
        showDeleteConfirmation,
        isDeleting,
        isRemoved,
    ) { (entry, input), status, confirmDelete, deleting, removed ->
        val coin = entry.coin
        val symbol = entry.currencySymbol
        val validation = AssetAmountValidator.validate(input)
        val estimated = EstimatedValueCalculator.compute(
            estimatedAmount(validation),
            coin?.currentPrice,
        )
        EditAssetUiState(
            isLoading = !status.isLoaded && !status.isNotFound,
            isNotFound = status.isNotFound,
            coinName = coin?.name.orEmpty(),
            coinSymbol = coin?.symbol ?: coinId.uppercase(Locale.US),
            coinImageUrl = coin?.imageUrl,
            currentPriceText = coin?.currentPrice?.let { MoneyFormat.format(it, symbol) },
            changeText = coin?.let { MoneyFormat.percent(it.priceChangePercentage24h) },
            isPriceUp = (coin?.priceChangePercentage24h ?: 0.0) >= 0.0,
            amountInput = input,
            amountError = amountInputError(validation),
            estimatedValueText = estimated?.let { MoneyFormat.format(it, symbol) },
            isSaveEnabled = validation is AssetAmountValidation.Valid &&
                !status.isSaving && !status.isSaved && !deleting,
            isSaving = status.isSaving,
            isSaved = status.isSaved,
            showDeleteConfirmation = confirmDelete,
            isDeleting = deleting,
            isRemoved = removed,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditAssetUiState(isLoading = true),
    )

    fun onAmountChange(raw: String) {
        amountInput.update { AssetAmountValidator.sanitize(raw) }
    }

    /** Exact-replace semantics (ADR-017): setHoldingAmount overwrites the stored amount. */
    fun onSave() {
        val validation = AssetAmountValidator.validate(amountInput.value)
        if (validation !is AssetAmountValidation.Valid) return
        viewModelScope.launch {
            isSaving.update { true }
            portfolioRepository.setHoldingAmount(coinId, validation.amount)
            isSaving.update { false }
            isSaved.update { true }
        }
    }

    fun onDeleteClick() {
        showDeleteConfirmation.update { true }
    }

    fun onDismissDelete() {
        showDeleteConfirmation.update { false }
    }

    fun onConfirmDelete() {
        if (isRemoved.value) return
        viewModelScope.launch {
            isDeleting.update { true }
            portfolioRepository.removeHolding(coinId)
            isDeleting.update { false }
            isRemoved.update { true }
        }
    }
}
