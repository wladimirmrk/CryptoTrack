package com.cryptotrack.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.cryptotrack.core.navigation.AboutRoute
import com.cryptotrack.core.navigation.AboutTopic
import com.cryptotrack.core.navigation.AboutTopicRoute
import com.cryptotrack.core.navigation.SettingsRoute
import com.cryptotrack.feature.settings.presentation.AboutScreen
import com.cryptotrack.feature.settings.presentation.AboutTopicScreen
import com.cryptotrack.feature.settings.presentation.SettingsScreen

/**
 * Контракт навигации фичи (ADR-004): :app собирает граф, фича не знает о других фичах.
 * About-экраны — цели строки About и разделов Terms/Privacy/Licenses (допущение A-4).
 */
fun NavGraphBuilder.settingsGraph(
    onBack: () -> Unit,
    onAboutClick: () -> Unit,
    onAboutTopicClick: (AboutTopic) -> Unit,
) {
    composable<SettingsRoute> {
        SettingsScreen(
            onBack = onBack,
            onAboutClick = onAboutClick,
        )
    }
    composable<AboutRoute> {
        AboutScreen(
            onBack = onBack,
            onTopicClick = onAboutTopicClick,
        )
    }
    composable<AboutTopicRoute> { entry ->
        AboutTopicScreen(
            topic = entry.toRoute<AboutTopicRoute>().topic,
            onBack = onBack,
        )
    }
}
