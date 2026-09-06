package com.cryptotrack.feature.coindetails.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.CoinDetailsRoute
import com.cryptotrack.feature.coindetails.presentation.CoinDetailsScreen

fun NavGraphBuilder.coinDetailsGraph(
    onBack: () -> Unit,
    onAddToPortfolio: (String) -> Unit,
) {
    composable<CoinDetailsRoute> {
        CoinDetailsScreen(
            onBack = onBack,
            onAddToPortfolio = onAddToPortfolio,
        )
    }
}
