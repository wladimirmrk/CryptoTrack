package com.cryptotrack.core.model

/**
 * Таймфреймы графика Coin Details (макет: 1H 24H 7D 1M 1Y ALL).
 * apiDays — параметр /market_chart; HOUR_1 и HOUR_24 оба смотрят последние сутки,
 * HOUR_1 дополнительно обрезается до 60 минут на уровне источника данных.
 */
enum class ChartRange(
    val apiDays: String,
    val label: String,
) {
    HOUR_1(apiDays = "1", label = "1H"),
    HOUR_24(apiDays = "1", label = "24H"),
    DAY_7(apiDays = "7", label = "7D"),
    MONTH_1(apiDays = "30", label = "1M"),
    YEAR_1(apiDays = "365", label = "1Y"),
    ALL(apiDays = "max", label = "ALL"),
}
