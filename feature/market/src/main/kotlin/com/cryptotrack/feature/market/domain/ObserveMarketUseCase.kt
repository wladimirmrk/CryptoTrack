package com.cryptotrack.feature.market.domain

import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.Coin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Снимок маркет-данных в валюте настроек + время последнего обновления кэша. */
data class MarketSnapshot(
    val currencySymbol: String,
    val coins: List<Coin>,
    val lastUpdatedMillis: Long?,
)

/**
 * UI читает только кэш Room (offline-first, ADR-005). Смена валюты в настройках
 * автоматически переключает поток данных.
 */
class ObserveMarketUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<MarketSnapshot> = settingsRepository.settings
        .map { it.currency }
        .distinctUntilChanged()
        .flatMapLatest { currency ->
            combine(
                marketRepository.observeMarkets(currency.apiCode),
                marketRepository.observeMarketLastUpdated(currency.apiCode),
            ) { coins, lastUpdated ->
                MarketSnapshot(
                    currencySymbol = currency.symbol,
                    coins = coins,
                    lastUpdatedMillis = lastUpdated,
                )
            }
        }
}
