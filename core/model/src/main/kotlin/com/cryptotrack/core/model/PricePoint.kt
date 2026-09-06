package com.cryptotrack.core.model

import java.math.BigDecimal

/** Точка истории цены для графика. */
data class PricePoint(
    val timestampMillis: Long,
    val price: BigDecimal,
)
