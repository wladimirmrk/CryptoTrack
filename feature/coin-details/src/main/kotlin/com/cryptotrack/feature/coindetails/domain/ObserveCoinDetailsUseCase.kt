package com.cryptotrack.feature.coindetails.domain

import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.CoinDetails
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class CoinDetailsSnapshot(
    val currencySymbol: String,
    val details: CoinDetails?,
    val lastUpdatedMillis: Long?,
)

/** Детали коина из кэша; смена валюты настроек переключает поток (offline-first, ADR-005). */
class ObserveCoinDetailsUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(coinId: String): Flow<CoinDetailsSnapshot> = settingsRepository.settings
        .map { it.currency }
        .distinctUntilChanged()
        .flatMapLatest { currency ->
            combine(
                marketRepository.observeCoinDetails(coinId, currency.apiCode),
                marketRepository.observeMarketLastUpdated(currency.apiCode),
            ) { details, lastUpdated ->
                CoinDetailsSnapshot(
                    currencySymbol = currency.symbol,
                    details = details,
                    lastUpdatedMillis = lastUpdated,
                )
            }
        }
}
