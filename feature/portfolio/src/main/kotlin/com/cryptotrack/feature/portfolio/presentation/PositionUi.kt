package com.cryptotrack.feature.portfolio.presentation

/** Строка актива портфеля (макет: имя, количество, USD-стоимость, 24h%). */
data class PositionUi(
    val coinId: String,
    val name: String,
    val symbol: String,
    val imageUrl: String?,
    val amountText: String,
    val valueText: String,
    val changeText: String,
    val isUp: Boolean,
)
