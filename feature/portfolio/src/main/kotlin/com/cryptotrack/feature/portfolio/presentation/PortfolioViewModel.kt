package com.cryptotrack.feature.portfolio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.onFailure
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.feature.portfolio.domain.ObservePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
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

data class PortfolioUiState(
    val coinId: String = "",
    val isLoading: Boolean = true,
    val totalBalanceText: String = "",
    val change24hText: String = "",
    val isUp: Boolean = true,
    val allocations: List<AllocationUi> = emptyList(),
    val positions: List<PositionUi> = emptyList(),
    val error: AppError? = null,
    val isRefreshing: Boolean = false,
    val lastUpdatedText: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && positions.isEmpty()
    val isOffline: Boolean get() = error != null && positions.isNotEmpty()
    val showError: Boolean get() = error != null && positions.isEmpty()
}

data class AllocationUi(
    val coinId: String,
    val symbol: String,
    val percent: Double,
)

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    observePortfolio: ObservePortfolioUseCase,
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    val uiState: StateFlow<PortfolioUiState> = combine(
        observePortfolio(),
        isRefreshing,
        refreshError,
    ) { snapshot, refreshing, error ->
        val summary = snapshot.summary
        val symbol = snapshot.currencySymbol
        PortfolioUiState(
            isLoading = snapshot.positions.isEmpty() && snapshot.lastUpdatedMillis == null &&
                error == null && refreshing,
            totalBalanceText = com.cryptotrack.core.common.format.MoneyFormat.format(summary.totalBalanceUsd, symbol),
            change24hText = com.cryptotrack.core.common.format.MoneyFormat.format(summary.change24hUsd, symbol) +
                " (" + com.cryptotrack.core.common.format.MoneyFormat.percent(summary.change24hPercent) + ")",
            isUp = summary.change24hUsd.signum() >= 0,
            allocations = summary.allocations.map { AllocationUi(it.coinId, it.symbol, it.percent) },
            positions = snapshot.positions.map { position ->
                PositionUi(
                    coinId = position.coin.id,
                    name = position.coin.name,
                    symbol = position.coin.symbol,
                    imageUrl = position.coin.imageUrl,
                    amountText = "${position.amount.toPlainString()} ${position.coin.symbol}",
                    valueText = com.cryptotrack.core.common.format.MoneyFormat.format(position.valueUsd, symbol),
                    changeText = com.cryptotrack.core.common.format.MoneyFormat.percent(
                        position.coin.priceChangePercentage24h,
                    ),
                    isUp = position.coin.priceChangePercentage24h >= 0,
                )
            },
            error = error,
            isRefreshing = refreshing,
            lastUpdatedText = snapshot.lastUpdatedMillis?.let { timeFormat.format(Date(it)) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PortfolioUiState(),
    )

    init {
        onRefresh()
    }

    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.update { true }
            refreshError.update { null }
            val currency = settingsRepository.settings.first().currency
            marketRepository.refreshMarkets(currency.apiCode)
                .onFailure { error -> refreshError.update { error } }
            isRefreshing.update { false }
        }
    }
}
