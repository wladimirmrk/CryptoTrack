package com.cryptotrack.feature.market.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.common.result.onFailure
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.feature.market.domain.MarketFilter
import com.cryptotrack.feature.market.domain.ObserveMarketUseCase
import com.cryptotrack.feature.market.domain.RefreshMarketUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MarketViewModel @Inject constructor(
    observeMarket: ObserveMarketUseCase,
    private val refreshMarket: RefreshMarketUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val filterFlow = MutableStateFlow(MarketFilter())
    private val isRefreshing = MutableStateFlow(false)
    private val refreshError = MutableStateFlow<AppError?>(null)

    private val timeFormat = SimpleDateFormat("h:mm a", Locale.US)

    /**
     * Автообновление маркет-данных (допущение A-7): периодический тик раз в
     * settings.refreshInterval. Тикер живёт в upstream uiState, поэтому активен
     * только при активной подписке UI (SharingStarted.WhileSubscribed); смена
     * интервала перестраивает таймер (flatMapLatest). Задержка стоит ПЕРЕД
     * первым тиком — первый refresh выполняет init-вызов, тикер его не дублирует.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val autoRefreshTicker = settingsRepository.settings
        .map { it.refreshInterval }
        .distinctUntilChanged()
        .flatMapLatest { interval ->
            flow {
                while (true) {
                    delay(interval.millis)
                    emit(Unit)
                }
            }
        }
        .onEach { onRefresh(force = true) }
        .onStart { emit(Unit) } // разблокирует combine при подписке, refresh не запуская

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
    }
        .combine(autoRefreshTicker) { state, _ -> state }
        .stateIn(
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
