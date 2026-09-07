package com.cryptotrack.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.cryptotrack.core.navigation.SettingsRoute
import com.cryptotrack.feature.settings.presentation.SettingsScreen

/**
 * Контракт навигации фичи (ADR-004): :app собирает граф, фича не знает о других фичах.
 */
fun NavGraphBuilder.settingsGraph(
    onBack: () -> Unit,
) {
    composable<SettingsRoute> {
        SettingsScreen(onBack = onBack)
    }
}
