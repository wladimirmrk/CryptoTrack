package com.cryptotrack.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Статус API Connection (макет Settings: «Connected (Latency: 42ms)»; допущение A-8). */
sealed interface ApiStatus {
    data object Checking : ApiStatus
    data class Connected(val latencyMs: Long) : ApiStatus
    data object Unavailable : ApiStatus
}

/** Состояние экрана Settings (макет Settings): значения Preferences + статус API. */
data class SettingsUiState(
    val currency: Currency = Currency.USD,
    val isDarkMode: Boolean = false,
    val refreshInterval: RefreshInterval = RefreshInterval.FIFTEEN_SECONDS,
    val apiStatus: ApiStatus = ApiStatus.Checking,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val marketRepository: MarketRepository,
) : ViewModel() {

    /**
     * API Connection (допущение A-8): health-check при открытии экрана и далее
     * периодически, пока экран подписан (WhileSubscribed останавливает опрос).
     */
    private val apiStatus = flow {
        while (true) {
            val status = when (val result = marketRepository.pingApi()) {
                is Result.Success -> ApiStatus.Connected(result.data)
                is Result.Failure -> ApiStatus.Unavailable
            }
            emit(status)
            delay(API_POLL_INTERVAL_MILLIS)
        }
    }

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.settings,
        apiStatus,
    ) { settings, status ->
        SettingsUiState(
            currency = settings.currency,
            isDarkMode = settings.isDarkMode,
            refreshInterval = settings.refreshInterval,
            apiStatus = status,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun onCurrencySelect(currency: Currency) {
        viewModelScope.launch {
            settingsRepository.setCurrency(currency)
        }
    }

    fun onDarkModeToggle(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkMode(enabled)
        }
    }

    fun onRefreshIntervalSelect(interval: RefreshInterval) {
        viewModelScope.launch {
            settingsRepository.setRefreshInterval(interval)
        }
    }

    private companion object {
        const val API_POLL_INTERVAL_MILLIS = 30_000L
    }
}
