package com.cryptotrack.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.SearchRoute
import com.cryptotrack.feature.search.presentation.SearchScreen

fun NavGraphBuilder.searchGraph(
    onBack: () -> Unit,
    onCoinClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    composable<SearchRoute> {
        SearchScreen(
            onBack = onBack,
            onCoinClick = onCoinClick,
            onSettingsClick = onSettingsClick,
        )
    }
}
