package com.cryptotrack.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Холдинг портфеля (ADR-013: только локально). BigDecimal хранится строкой. */
@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val coinId: String,
    val amount: String,
    val updatedAtMillis: Long,
)
