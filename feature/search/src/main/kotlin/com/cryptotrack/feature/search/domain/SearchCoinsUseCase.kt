package com.cryptotrack.feature.search.domain

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Coin
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Поиск по CoinGecko с debounce ввода; цены обогащаются из кэша (см. MarketRepository.searchCoins).
 */
class SearchCoinsUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(FlowPreview::class)
    fun observeSearchResults(queryFlow: Flow<String>): Flow<SearchState> = queryFlow
        .debounce(DEBOUNCE_MILLIS)
        .distinctUntilChanged()
        .map { query -> search(query) }

    private suspend fun search(query: String): SearchState {
        if (query.isBlank()) return SearchState.Idle
        val currency = settingsRepository.settings.first().currency
        return when (val result = marketRepository.searchCoins(query.trim(), currency.apiCode)) {
            is Result.Success -> SearchState.Results(result.data)
            is Result.Failure -> SearchState.Error(result.error)
        }
    }

    companion object {
        const val DEBOUNCE_MILLIS = 300L
    }
}

sealed interface SearchState {
    data object Idle : SearchState

    data class Results(val coins: List<Coin>) : SearchState

    data class Error(val error: com.cryptotrack.core.common.result.AppError) : SearchState
}
