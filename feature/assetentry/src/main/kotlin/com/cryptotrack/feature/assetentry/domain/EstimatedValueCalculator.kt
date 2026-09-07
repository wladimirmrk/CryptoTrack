package com.cryptotrack.feature.assetentry.domain

import java.math.BigDecimal

/**
 * Live "Estimated Total Value" (Add to Portfolio / Edit Asset mockups):
 * amount x current market price.
 *
 * Returns null (rendered as an em dash) when the price is unavailable — the coin is
 * outside the cached market list (top-50, Phase 7 limitation) or the amount is not a
 * valid positive number yet. Saving stays allowed: the value resolves once the coin
 * appears in the cache.
 */
object EstimatedValueCalculator {

    fun compute(
        amount: BigDecimal?,
        currentPrice: BigDecimal?,
    ): BigDecimal? {
        if (amount == null || currentPrice == null) return null
        return currentPrice.multiply(amount)
    }
}
