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

/** Разделы экрана About (макет не задаёт контент — статичные placeholder-экраны, допущение A-4). */
@Serializable
enum class AboutTopic {
    Terms,
    Privacy,
    Licenses,
}

@Serializable
data object AboutRoute

@Serializable
data class AboutTopicRoute(val topic: AboutTopic)

@Serializable
data class AddAssetRoute(val coinId: String)

@Serializable
data class EditAssetRoute(val coinId: String)
