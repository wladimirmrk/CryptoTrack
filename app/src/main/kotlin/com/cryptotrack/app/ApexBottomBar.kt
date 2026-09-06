package com.cryptotrack.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.cryptotrack.core.navigation.FavoritesRoute
import kotlin.reflect.KClass

/** Bottom navigation: активная иконка — Primary, неактивная — Secondary Gray (design-analysis §1.5). */
@Composable
fun ApexBottomBar(
    isTopLevelDestination: (KClass<*>) -> Boolean,
    onDestinationSelected: (TopDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        topLevelDestinations.forEach { destination ->
            val selected = isTopLevelDestination(destination.routeClass)
            NavigationBarItem(
                selected = selected,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Icon(
                        imageVector = selectedIcon(destination, selected),
                        contentDescription = destination.label,
                    )
                },
                label = { Text(text = destination.label) },
            )
        }
    }
}

private fun selectedIcon(destination: TopDestination, selected: Boolean): ImageVector =
    if (selected && destination.routeClass == FavoritesRoute::class) {
        Icons.Filled.Star
    } else {
        destination.icon
    }
