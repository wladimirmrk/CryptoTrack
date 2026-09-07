package com.cryptotrack.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Currency
import com.cryptotrack.core.model.RefreshInterval
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Состояние экрана Settings (макет Settings): значения секции Preferences. */
data class SettingsUiState(
    val currency: Currency = Currency.USD,
    val isDarkMode: Boolean = false,
    val refreshInterval: RefreshInterval = RefreshInterval.FIFTEEN_SECONDS,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { settings ->
            SettingsUiState(
                currency = settings.currency,
                isDarkMode = settings.isDarkMode,
                refreshInterval = settings.refreshInterval,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsUiState(),
        )

    fun onCurrencySelect(currency: Currency) {
        viewModelScope.launch {
            settingsRepository.setCurrency(currency)
        }
    }
}
