package com.cryptotrack.core.model

import java.math.BigDecimal

/**
 * Позиция портфеля: холдинг × текущая цена коина (агрегаты считает use case, ADR-013).
 */
data class PortfolioPosition(
    val coin: Coin,
    val amount: BigDecimal,
    val valueUsd: BigDecimal,
)

data class Allocation(
    val coinId: String,
    val symbol: String,
    val percent: Double,
)

/**
 * Сводка портфеля для карточки Total Balance и секции Allocation (макет Portfolio Overview).
 */
data class PortfolioSummary(
    val totalBalanceUsd: BigDecimal,
    val change24hUsd: BigDecimal,
    val change24hPercent: Double,
    val allocations: List<Allocation>,
)
