package com.cryptotrack.feature.assetentry.domain

import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Coin
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

/**
 * Coin context for the Add and Edit forms: market data observed in the display currency
 * setting (offline-first, ADR-005). A null emission means the coin is not in the cached
 * market list — the forms keep working with a dash price placeholder.
 */
class ObserveEntryCoinUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(coinId: String): Flow<Coin?> = settingsRepository.settings
        .map { it.currency }
        .distinctUntilChanged()
        .flatMapLatest { currency ->
            marketRepository.observeCoin(coinId, currency.apiCode)
        }
}
