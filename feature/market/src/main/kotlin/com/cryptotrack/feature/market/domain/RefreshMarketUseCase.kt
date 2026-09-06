package com.cryptotrack.feature.market.domain

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Обновление маркет-списка из сети (пишет в кэш).
 * force=false — только если кэш старше TTL; pull-to-refresh вызывает с force=true.
 */
class RefreshMarketUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(force: Boolean = false): Result<Unit> {
        val currency = settingsRepository.settings.first().currency
        if (!force) {
            val lastUpdated = marketRepository.observeMarketLastUpdated(currency.apiCode).first()
            val age = System.currentTimeMillis() - (lastUpdated ?: 0L)
            if (age < TTL_MILLIS) return Result.Success(Unit)
        }
        // Первая страница всегда; следующие — по пагинации списка.
        return marketRepository.refreshMarkets(currency.apiCode)
    }

    companion object {
        const val TTL_MILLIS = 60_000L
    }
}
