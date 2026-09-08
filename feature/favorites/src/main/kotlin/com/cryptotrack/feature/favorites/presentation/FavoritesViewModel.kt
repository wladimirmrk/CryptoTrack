package com.cryptotrack.feature.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Coin
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FavoritesUiState(
    val isLoading: Boolean = true,
    val coins: List<Coin> = emptyList(),
    val currencySymbol: String = "",
) {
    val countText: String get() = "${coins.size} Assets"
    val isEmpty: Boolean get() = !isLoading && coins.isEmpty()
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoritesRepository: FavoritesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<FavoritesUiState> = settingsRepository.settings
        .map { it.currency }
        .flatMapLatest { currency ->
            favoritesRepository.observeFavorites(currency.apiCode).map { coins ->
                FavoritesUiState(isLoading = false, coins = coins, currencySymbol = currency.symbol)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(isLoading = true),
        )

    fun onFavoriteToggle(coinId: String) {
        viewModelScope.launch {
            favoritesRepository.setFavorite(coinId, isFavorite = false)
        }
    }
}
