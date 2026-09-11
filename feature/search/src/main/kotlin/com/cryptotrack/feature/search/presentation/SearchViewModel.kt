package com.cryptotrack.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cryptotrack.core.data.repository.FavoritesRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Coin
import com.cryptotrack.feature.search.domain.SearchState
import com.cryptotrack.feature.search.domain.SearchCoinsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val isLoading: Boolean = false,
    val results: List<Coin> = emptyList(),
    val currencySymbol: String = "",
    val isFavorite: (String) -> Boolean = { false },
) {
    val hasQuery: Boolean get() = query.isNotBlank()
    val isEmptyResults: Boolean get() = hasQuery && !isLoading && results.isEmpty()
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    searchCoins: SearchCoinsUseCase,
    private val favoritesRepository: FavoritesRepository,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val isLoading = MutableStateFlow(false)

    val uiState: StateFlow<SearchUiState> = combine(
        queryFlow,
        isLoading,
        favoritesRepository.observeFavoriteIds(),
        settingsRepository.settings,
        searchCoins.observeSearchResults(queryFlow),
    ) { query, loading, favoriteIds, settings, state ->
        SearchUiState(
            query = query,
            isLoading = state is SearchState.Idle && loading,
            results = (state as? SearchState.Results)?.coins ?: emptyList(),
            currencySymbol = settings.currency.symbol,
            isFavorite = { favoriteIds.contains(it) },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SearchUiState(),
    )

    fun onQueryChange(query: String) {
        queryFlow.update { query }
    }

    fun onFavoriteToggle(coinId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            favoritesRepository.setFavorite(coinId, isFavorite)
        }
    }
}
