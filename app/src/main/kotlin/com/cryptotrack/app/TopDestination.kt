package com.cryptotrack.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.cryptotrack.core.navigation.FavoritesRoute
import com.cryptotrack.core.navigation.MarketRoute
import com.cryptotrack.core.navigation.PortfolioRoute
import kotlin.reflect.KClass

/** Элемент bottom navigation (макеты: Market chart / Portfolio wallet / Favorites star). */
data class TopDestination(
    val routeClass: KClass<*>,
    val label: String,
    val icon: ImageVector,
)

val topLevelDestinations = listOf(
    TopDestination(MarketRoute::class, "Market", Icons.Filled.ShowChart),
    TopDestination(PortfolioRoute::class, "Portfolio", Icons.Filled.AccountBalanceWallet),
    TopDestination(FavoritesRoute::class, "Favorites", Icons.Filled.StarBorder),
)
