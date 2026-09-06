package com.cryptotrack.feature.coindetails.domain

import com.cryptotrack.core.common.result.Result
import com.cryptotrack.core.data.repository.MarketRepository
import com.cryptotrack.core.data.repository.SettingsRepository
import com.cryptotrack.core.model.ChartRange
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/** Обновление деталей и истории цены из сети (пишет в кэш). */
class RefreshCoinDataUseCase @Inject constructor(
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend fun details(coinId: String): Result<Unit> {
        val currency = settingsRepository.settings.first().currency
        return marketRepository.refreshCoinDetails(coinId, currency.apiCode)
    }

    suspend fun history(coinId: String, range: ChartRange): Result<Unit> {
        val currency = settingsRepository.settings.first().currency
        return marketRepository.refreshPriceHistory(coinId, currency.apiCode, range)
    }
}
