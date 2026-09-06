package com.cryptotrack.feature.market.presentation

import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.model.Coin
import com.cryptotrack.feature.market.domain.MarketFilter

/** Готовая к отрисовке строка маркет-списка (форматирование — в VM, тестируемо). */
data class CoinUi(
    val coinId: String,
    val name: String,
    val symbol: String,
    val imageUrl: String?,
    val priceText: String,
    val changeText: String,
    val isUp: Boolean,
    val isFavorite: Boolean,
)

/**
 * Состояние Market Home (макеты: Market Home / Loading / Error / Offline):
 * - isLoading: skeleton, пока кэш пуст и первая загрузка не завершена;
 * - error + пустой кэш → fullscreen ErrorState;
 * - error + кэш → offline-баннер поверх контента.
 */
data class MarketUiState(
    val isLoading: Boolean = true,
    val coins: List<CoinUi> = emptyList(),
    val error: AppError? = null,
    val isRefreshing: Boolean = false,
    val lastUpdatedText: String? = null,
    val filter: MarketFilter = MarketFilter(),
) {
    val isEmpty: Boolean get() = !isLoading && error == null && coins.isEmpty()
    val isOffline: Boolean get() = error != null && coins.isNotEmpty()
    val showError: Boolean get() = error != null && coins.isEmpty()
}

internal fun Coin.toCoinUi(isFavorite: Boolean): CoinUi = CoinUi(
    coinId = id,
    name = name,
    symbol = symbol,
    imageUrl = imageUrl,
    priceText = com.cryptotrack.core.common.format.MoneyFormat.usd(currentPrice),
    changeText = com.cryptotrack.core.common.format.MoneyFormat.percent(priceChangePercentage24h),
    isUp = priceChangePercentage24h >= 0,
    isFavorite = isFavorite,
)
