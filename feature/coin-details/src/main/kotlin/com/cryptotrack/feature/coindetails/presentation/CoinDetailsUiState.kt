package com.cryptotrack.feature.coindetails.presentation

import com.cryptotrack.core.common.format.MoneyFormat
import com.cryptotrack.core.common.result.AppError
import com.cryptotrack.core.model.ChartRange
import com.cryptotrack.core.model.CoinDetails

/** Готовые к отрисовке данные экрана Coin Details (форматирование в VM, тестируемо). */
data class CoinDetailsUiState(
    val coinId: String = "",
    val isLoading: Boolean = true,
    val name: String = "",
    val symbol: String = "",
    val imageUrl: String? = null,
    val priceText: String = "",
    val changeText: String = "",
    val isUp: Boolean = true,
    val marketCapText: String? = null,
    val marketCapRank: Int? = null,
    val volumeText: String? = null,
    val supplyText: String? = null,
    val highText: String? = null,
    val lowText: String? = null,
    val about: String? = null,
    val chartPoints: List<Double> = emptyList(),
    val isChartUp: Boolean = true,
    val selectedRange: ChartRange = ChartRange.HOUR_24,
    val isChartLoading: Boolean = false,
    val isFavorite: Boolean = false,
    val error: AppError? = null,
    val isRefreshing: Boolean = false,
    val lastUpdatedText: String? = null,
) {
    val detailsMissing: Boolean get() = priceText.isEmpty()
    val isEmpty: Boolean get() = !isLoading && detailsMissing
    val isOffline: Boolean get() = error != null && !detailsMissing
    val showError: Boolean get() = error != null && detailsMissing
}

internal fun CoinDetails.toUiState(currencySymbol: String): CoinDetailsUiState = CoinDetailsUiState(
    name = name,
    symbol = symbol,
    imageUrl = imageUrl,
    priceText = MoneyFormat.format(currentPrice, currencySymbol),
    changeText = MoneyFormat.percent(priceChangePercentage24h),
    isUp = priceChangePercentage24h >= 0,
    marketCapText = marketCap?.let { MoneyFormat.compact(it, currencySymbol) },
    marketCapRank = marketCapRank,
    volumeText = totalVolume?.let { MoneyFormat.compact(it, currencySymbol) },
    supplyText = circulatingSupply?.let { "${MoneyFormat.compact(it, currencySymbol)} $symbol" },
    highText = high24h?.let { MoneyFormat.format(it, currencySymbol) },
    lowText = low24h?.let { MoneyFormat.format(it, currencySymbol) },
    about = description.takeIf { it.isNotBlank() },
)
