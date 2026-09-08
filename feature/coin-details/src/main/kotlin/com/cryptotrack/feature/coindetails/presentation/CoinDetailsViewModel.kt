package com.cryptotrack.feature.coindetails.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.onFailure
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.navigation.CoinDetailsRoute
import com.cryptotrack.feature.coindetails.domain.ObserveCoinDetailsUseCase
import com.cryptotrack.feature.coindetails.domain.ObservePriceHistoryUseCase
import com.cryptotrack.feature.coindetails.domain.RefreshCoinDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CoinDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeDetails: ObserveCoinDetailsUseCase,
    observeHistory: ObservePriceHistoryUseCase,
    private val refreshCoinData: RefreshCoinDataUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    // Типобезопасная навигация кладёт аргументы в SavedStateHandle по имени свойства.
    private val coinId: String = checkNotNull(savedStateHandle["coinId"])

    private val rangeFlow = MutableStateFlow(ChartRange.HOUR_24)
    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<AppError?>(null)
    private val isHistoryLoading = MutableStateFlow(false)

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    private val dataFlow = combine(
        observeDetails(coinId),
        observeHistory(coinId, rangeFlow),
    ) { snapshot, points ->
        snapshot to points
    }

    private val statusFlow = combine(
        favoritesRepository.observeIsFavorite(coinId),
        isRefreshing,
        refreshError,
        isHistoryLoading,
        rangeFlow,
    ) { isFavorite, refreshing, error, historyLoading, range ->
        CombinedStatus(isFavorite, refreshing, error, historyLoading, range)
    }

    val uiState: StateFlow<CoinDetailsUiState> = combine(dataFlow, statusFlow) { (snapshot, points), status ->
        val base = (snapshot.details?.toUiState(snapshot.currencySymbol) ?: CoinDetailsUiState())
            .copy(coinId = coinId)
        base.copy(
            isLoading = base.detailsMissing && snapshot.lastUpdatedMillis == null &&
                status.error == null && status.isRefreshing,
            chartPoints = points.map { it.price.toDouble() },
            isChartUp = points.size > 1 && points.last().price >= points.first().price,
            isChartLoading = status.isHistoryLoading,
            selectedRange = status.range,
            isFavorite = status.isFavorite,
            error = status.error,
            isRefreshing = status.isRefreshing,
            lastUpdatedText = snapshot.lastUpdatedMillis?.let { timeFormat.format(Date(it)) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CoinDetailsUiState(),
    )

    private data class CombinedStatus(
        val isFavorite: Boolean,
        val isRefreshing: Boolean,
        val error: AppError?,
        val isHistoryLoading: Boolean,
        val range: ChartRange,
    )

    init {
        onRefresh()
    }

    /** Обновление деталей + истории; смена таймфрейма подтягивает историю через rangeFlow. */
    fun onRefresh() {
        viewModelScope.launch {
            isRefreshing.update { true }
            refreshError.update { null }
            refreshCoinData.details(coinId).onFailure { error -> refreshError.update { error } }
            refreshCoinData.history(coinId, rangeFlow.value)
                .onFailure { error -> refreshError.update { error } }
            isRefreshing.update { false }
        }
    }

    fun onRangeSelect(range: ChartRange) {
        if (range == rangeFlow.value) return
        rangeFlow.update { range }
        viewModelScope.launch {
            isHistoryLoading.update { true }
            refreshCoinData.history(coinId, range).onFailure { error -> refreshError.update { error } }
            isHistoryLoading.update { false }
        }
    }

    fun onFavoriteToggle() {
        viewModelScope.launch {
            favoritesRepository.setFavorite(coinId, !uiState.value.isFavorite)
        }
    }
}
