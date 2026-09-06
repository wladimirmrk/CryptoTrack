package com.cryptotrack.core.model

import java.math.BigDecimal

/**
 * Холдинг пользователя — локальная запись портфеля (ADR-013: только на устройстве).
 * amount > 0.
 */
data class Holding(
    val coinId: String,
    val amount: BigDecimal,
    val updatedAtMillis: Long,
)
