package com.cryptotrack.core.database.entity

import androidx.room.Entity

/** История цены для графика, per-(коин, валюта, таймфрейм). */
@Entity(
    tableName = "price_history",
    primaryKeys = ["coinId", "currency", "range", "timestampMillis"],
)
data class PriceHistoryEntity(
    val coinId: String,
    val currency: String,
    val range: String,
    val timestampMillis: Long,
    val price: String,
)
