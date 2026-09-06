package com.cryptotrack.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.cryptotrack.core.navigation.AddAssetRoute
import com.cryptotrack.core.navigation.CoinDetailsRoute
import com.cryptotrack.core.navigation.EditAssetRoute
import com.cryptotrack.core.navigation.FavoritesRoute
import com.cryptotrack.core.navigation.MarketRoute
import com.cryptotrack.core.navigation.PortfolioRoute
import com.cryptotrack.core.navigation.SearchRoute
import com.cryptotrack.core.navigation.SettingsRoute

/**
 * Единый NavHost приложения (ADR-004). Топ-уровень — Market/Portfolio/Favorites (bottom bar);
 * второй уровень — Details/Search/Settings/AddAsset/EditAsset (аргументы — только id).
 * В Phase 4–9 composable-заглушки заменяются графами feature-модулей.
 */
@Composable
fun CryptoTrackNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        modifier = modifier,
        bottomBar = {
            val isTopLevel = topLevelDestinations.any { destination ->
                currentDestination?.hierarchy?.any { it.hasRoute(destination.routeClass) } == true
            }
            if (isTopLevel) {
                ApexBottomBar(
                    isTopLevelDestination = { routeClass ->
                        currentDestination?.hierarchy?.any { it.hasRoute(routeClass) } == true
                    },
                    onDestinationSelected = { destination ->
                        navController.navigate(destination.routeInstance()) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MarketRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<MarketRoute> {
                PlaceholderScreen(title = "Market", phaseHint = "Phase 4")
            }
            composable<PortfolioRoute> {
                PlaceholderScreen(title = "Portfolio", phaseHint = "Phase 7")
            }
            composable<FavoritesRoute> {
                PlaceholderScreen(title = "Favorites", phaseHint = "Phase 6")
            }
            composable<CoinDetailsRoute> { entry ->
                val route = entry.toRoute<CoinDetailsRoute>()
                PlaceholderScreen(title = "Coin Details: ${route.coinId}", phaseHint = "Phase 5")
            }
            composable<SearchRoute> {
                PlaceholderScreen(title = "Search", phaseHint = "Phase 6")
            }
            composable<SettingsRoute> {
                PlaceholderScreen(title = "Settings", phaseHint = "Phase 9")
            }
            composable<AddAssetRoute> { entry ->
                val route = entry.toRoute<AddAssetRoute>()
                PlaceholderScreen(title = "Add Asset: ${route.coinId}", phaseHint = "Phase 8")
            }
            composable<EditAssetRoute> { entry ->
                val route = entry.toRoute<EditAssetRoute>()
                PlaceholderScreen(title = "Edit Asset: ${route.coinId}", phaseHint = "Phase 8")
            }
        }
    }
}

private fun TopDestination.routeInstance(): Any = when (routeClass) {
    MarketRoute::class -> MarketRoute
    PortfolioRoute::class -> PortfolioRoute
    FavoritesRoute::class -> FavoritesRoute
    else -> error("Unknown top-level destination: $routeClass")
}
