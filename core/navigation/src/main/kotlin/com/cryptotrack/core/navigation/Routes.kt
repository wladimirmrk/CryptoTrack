package com.cryptotrack.core.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe маршруты приложения (ADR-004). Между экранами передаются только id.
 * Сплэш — стартовая destination в :app (ADR-010).
 */
@Serializable
data object SplashRoute

@Serializable
data object MarketRoute

@Serializable
data object PortfolioRoute

@Serializable
data object FavoritesRoute

@Serializable
data class CoinDetailsRoute(val coinId: String)

@Serializable
data object SearchRoute

@Serializable
data object SettingsRoute

@Serializable
data class AddAssetRoute(val coinId: String)

@Serializable
data class EditAssetRoute(val coinId: String)
