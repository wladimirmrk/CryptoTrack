package com.cryptotrack.feature.portfolio.domain

import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.PortfolioRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.PortfolioPosition
import com.cryptotrack.core.model.PortfolioSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Портфель: холдинги × цены кэша. UI читает Flow (offline-first, ADR-005).
 * Коины, отсутствующие в кэше маркет-списка (вне top-50), в агрегаты не попадают —
 * ограничение первой версии (см. development-plan Phase 7).
 */
class ObservePortfolioUseCase @Inject constructor(
    private val portfolioRepository: PortfolioRepository,
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<PortfolioSnapshot> = settingsRepository.settings
        .map { it.currency }
        .distinctUntilChanged()
        .flatMapLatest { currency ->
            combine(
                portfolioRepository.observeHoldings(),
                marketRepository.observeMarkets(currency.apiCode),
                marketRepository.observeMarketLastUpdated(currency.apiCode),
            ) { holdings, coins, lastUpdated ->
                val result = PortfolioCalculator.compute(holdings, coins)
                PortfolioSnapshot(
                    currencySymbol = currency.symbol,
                    positions = result.positions,
                    summary = result.summary,
                    lastUpdatedMillis = lastUpdated,
                )
            }
        }
}

data class PortfolioSnapshot(
    val currencySymbol: String,
    val positions: List<PortfolioPosition>,
    val summary: PortfolioSummary,
    val lastUpdatedMillis: Long?,
)
