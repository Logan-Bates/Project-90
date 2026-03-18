package com.logan.project90.ui.components

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.logan.project90.navigation.AppDestination

@Composable
fun AppBottomBar(
    currentDestination: NavDestination?,
    onNavigate: (AppDestination) -> Unit
) {
    NavigationBar {
        AppDestination.topLevelDestinations.forEach { destination ->
            NavigationBarItem(
                selected = currentDestination
                    ?.hierarchy
                    ?.any { it.route == destination.route } == true,
                onClick = { onNavigate(destination) },
                icon = {},
                label = { Text(text = destination.label) },
                alwaysShowLabel = true
            )
        }
    }
}

private val AppDestination.label: String
    get() = when (this) {
        AppDestination.Today -> "Today"
        AppDestination.Analytics -> "Analytics"
        AppDestination.Experiment -> "Experiment"
        else -> route
    }
