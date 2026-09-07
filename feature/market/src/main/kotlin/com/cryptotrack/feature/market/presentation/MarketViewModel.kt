package com.cryptotrack.feature.market.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.onFailure
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.feature.market.domain.MarketFilter
import com.cryptotrack.feature.market.domain.ObserveMarketUseCase
import com.cryptotrack.feature.market.domain.RefreshMarketUseCase
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
class MarketViewModel @Inject constructor(
    observeMarket: ObserveMarketUseCase,
    private val refreshMarket: RefreshMarketUseCase,
    private val favoritesRepository: FavoritesRepository,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(MarketFilter())
    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<AppError?>(null)

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    val uiState: StateFlow<MarketUiState> = combine(
        observeMarket(),
        filterFlow,
        favoritesRepository.observeFavoriteIds(),
        isRefreshing,
        refreshError,
    ) { snapshot, filter, favoriteIds, refreshing, error ->
        MarketUiState(
            isLoading = snapshot.coins.isEmpty() &&
                snapshot.lastUpdatedMillis == null &&
                error == null &&
                refreshing,
            coins = filter.apply(snapshot.coins).map { coin ->
                coin.toCoinUi(
                    isFavorite = favoriteIds.contains(coin.id),
                    currencySymbol = snapshot.currencySymbol,
                )
            },
            error = error,
            isRefreshing = refreshing,
            lastUpdatedText = snapshot.lastUpdatedMillis?.let { timeFormat.format(Date(it)) },
            filter = filter,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MarketUiState(),
    )

    init {
        onRefresh(force = false)
    }

    /** Pull-to-refresh: force=true игнорирует TTL; первая загрузка соблюдает его. */
    fun onRefresh(force: Boolean = true) {
        viewModelScope.launch {
            isRefreshing.update { true }
            refreshError.update { null }
            refreshMarket(force = force)
                .onFailure { error -> refreshError.update { error } }
            isRefreshing.update { false }
        }
    }

    fun onApplyFilter(filter: MarketFilter) {
        filterFlow.update { filter }
    }

    fun onFavoriteToggle(coinId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setFavorite(coinId, isFavorite)
        }
    }
}
