package com.cryptotrack.feature.portfolio.domain

import com.cryptotrack.core.model.Coin
import com.cryptotrack.core.model.Holding
import com.cryptotrack.core.model.PortfolioPosition
import com.cryptotrack.core.model.PortfolioSummary
import java.math.BigDecimal
import java.math.MathContext

/** Чистая арифметика портфеля — приоритет юнит-тестов (Phase 7, development-plan). */
object PortfolioCalculator {

    fun compute(holdings: List<Holding>, coins: List<Coin>): PortfolioResult {
        val positions = holdings.mapNotNull { holding ->
            val coin = coins.find { it.id == holding.coinId } ?: return@mapNotNull null
            PortfolioPosition(
                coin = coin,
                amount = holding.amount,
                valueUsd = coin.currentPrice.multiply(holding.amount, MathContext.DECIMAL32),
            )
        }

        val total = positions.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.valueUsd) }
        val change24hUsd = positions.fold(BigDecimal.ZERO) { acc, p ->
            val change = BigDecimal.valueOf(p.coin.priceChangePercentage24h / 100.0)
            acc.add(p.valueUsd.multiply(change, MathContext.DECIMAL32))
        }
        val yesterdayValue = total.subtract(change24hUsd)
        val changePercent = if (yesterdayValue.signum() > 0) {
            change24hUsd.divide(yesterdayValue, MathContext.DECIMAL32).toDouble() * 100.0
        } else {
            0.0
        }

        val allocations = positions.map { position ->
            val percent = if (total.signum() > 0) {
                position.valueUsd.divide(total, MathContext.DECIMAL32).toDouble() * 100.0
            } else {
                0.0
            }
            position.coin.id to percent
        }

        return PortfolioResult(
            positions = positions,
            summary = PortfolioSummary(
                totalBalanceUsd = total,
                change24hUsd = change24hUsd,
                change24hPercent = changePercent,
                allocations = allocations.map { (coinId, percent) ->
                    com.cryptotrack.core.model.Allocation(
                        coinId = coinId,
                        symbol = (positions.find { it.coin.id == coinId }?.coin?.symbol).orEmpty(),
                        percent = percent,
                    )
                },
            ),
        )
    }

    data class PortfolioResult(
        val positions: List<PortfolioPosition>,
        val summary: PortfolioSummary,
    )
}
