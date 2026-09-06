package com.cryptotrack.feature.market.domain

import com.cryptotrack.core.model.Coin

/**
 * Фильтры Market (макет Market Filters): Sort By + Order.
 * Сортировка клиентская — работает и офлайн поверх кэша (ADR-005).
 */
enum class MarketSortBy(val label: String) {
    MARKET_CAP("Market Cap"),
    PRICE("Price"),
    CHANGE_24H("24h Change"),
    ALPHABETICAL("Alphabetical"),
}

enum class SortOrder(val label: String) {
    DESCENDING("Descending"),
    ASCENDING("Ascending"),
}

data class MarketFilter(
    val sortBy: MarketSortBy = MarketSortBy.MARKET_CAP,
    val order: SortOrder = SortOrder.DESCENDING,
) {
    fun apply(coins: List<Coin>): List<Coin> {
        val comparator: Comparator<Coin> = when (sortBy) {
            MarketSortBy.MARKET_CAP -> compareBy { it.marketCap ?: java.math.BigDecimal.ZERO }
            MarketSortBy.PRICE -> compareBy { it.currentPrice }
            MarketSortBy.CHANGE_24H -> compareBy { it.priceChangePercentage24h }
            MarketSortBy.ALPHABETICAL -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        }
        val ordered = if (order == SortOrder.DESCENDING) comparator.reversed() else comparator
        return coins.sortedWith(ordered)
    }
}
