package com.cryptotrack.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.compose.rememberNavController
import com.cryptotrack.core.navigation.AddAssetRoute
import com.cryptotrack.core.navigation.CoinDetailsRoute
import com.cryptotrack.core.navigation.EditAssetRoute
import com.cryptotrack.core.navigation.FavoritesRoute
import com.cryptotrack.core.navigation.MarketRoute
import com.cryptotrack.core.navigation.PortfolioRoute
import com.cryptotrack.core.navigation.SearchRoute
import com.cryptotrack.core.navigation.SettingsRoute

/**
 * Единый NavHost приложения (ADR-004). Топ-уровень — Market/Portfolio/Favorites;
 * второй уровень — Details/Search/Settings/AddAsset/EditAsset (аргументы — только id).
 * В Phase 4–9 composable-заглушки заменяются графами feature-модулей.
 */
@Composable
fun CryptoTrackNavHost(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = MarketRoute,
        modifier = modifier,
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
