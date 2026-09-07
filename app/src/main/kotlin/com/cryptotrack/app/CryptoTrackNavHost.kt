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
import com.cryptotrack.core.navigation.AboutRoute
import com.cryptotrack.core.navigation.AboutTopicRoute
import com.cryptotrack.core.navigation.AddAssetRoute
import com.cryptotrack.core.navigation.CoinDetailsRoute
import com.cryptotrack.core.navigation.EditAssetRoute
import com.cryptotrack.core.navigation.FavoritesRoute
import com.cryptotrack.core.navigation.MarketRoute
import com.cryptotrack.feature.assetentry.navigation.addAssetGraph
import com.cryptotrack.feature.assetentry.navigation.editAssetGraph
import com.cryptotrack.feature.coindetails.navigation.coinDetailsGraph
import com.cryptotrack.feature.favorites.navigation.favoritesGraph
import com.cryptotrack.feature.portfolio.navigation.portfolioGraph
import com.cryptotrack.feature.search.navigation.searchGraph
import com.cryptotrack.feature.settings.navigation.settingsGraph
import com.cryptotrack.feature.market.navigation.marketGraph
import com.cryptotrack.core.navigation.PortfolioRoute
import com.cryptotrack.core.navigation.SearchRoute
import com.cryptotrack.core.navigation.SettingsRoute
import com.cryptotrack.core.navigation.SplashRoute

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
            startDestination = SplashRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<SplashRoute> {
                SplashScreen(
                    onDone = {
                        navController.navigate(MarketRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                        }
                    },
                )
            }
            marketGraph(
                onCoinClick = { coinId -> navController.navigate(CoinDetailsRoute(coinId)) },
                onSearchClick = { navController.navigate(SearchRoute) },
            )
            portfolioGraph(
                // Flow F1 (assumption A-15): tapping a portfolio row opens the Edit Asset
                // screen; Coin Details stays reachable from Market/Search/Favorites.
                onAssetClick = { coinId -> navController.navigate(EditAssetRoute(coinId)) },
                onSearchClick = { navController.navigate(SearchRoute) },
                onAddFirstAsset = { navController.navigate(SearchRoute) },
                onHowToFund = { navController.navigate(SettingsRoute) },
            )
            favoritesGraph(
                onCoinClick = { coinId -> navController.navigate(CoinDetailsRoute(coinId)) },
                onBrowseMarket = {
                    navController.navigate(MarketRoute) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onSearchClick = { navController.navigate(SearchRoute) },
            )
            coinDetailsGraph(
                onBack = { navController.popBackStack() },
                onAddToPortfolio = { coinId -> navController.navigate(AddAssetRoute(coinId)) },
            )
            searchGraph(
                onBack = { navController.popBackStack() },
                onCoinClick = { coinId -> navController.navigate(CoinDetailsRoute(coinId)) },
                onSettingsClick = { navController.navigate(SettingsRoute) },
            )
            settingsGraph(
                onBack = { navController.popBackStack() },
                onAboutClick = { navController.navigate(AboutRoute) },
                onAboutTopicClick = { topic -> navController.navigate(AboutTopicRoute(topic)) },
            )
            addAssetGraph(
                onDismiss = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(PortfolioRoute) {
                        // Flow F2 "Save → return to Portfolio": drop the sheet entry so
                        // back does not reopen the form; single top avoids a duplicate.
                        popUpTo<AddAssetRoute> { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
            editAssetGraph(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onRemoved = { navController.popBackStack() },
            )
        }
    }
}

private fun TopDestination.routeInstance(): Any = when (routeClass) {
    MarketRoute::class -> MarketRoute
    PortfolioRoute::class -> PortfolioRoute
    FavoritesRoute::class -> FavoritesRoute
    else -> error("Unknown top-level destination: $routeClass")
}
