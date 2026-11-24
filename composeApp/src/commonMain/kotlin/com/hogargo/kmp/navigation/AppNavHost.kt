package com.hogargo.kmp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hogargo.kmp.data.HogarGoRepository
import com.hogargo.kmp.ui.screens.AboutScreen
import com.hogargo.kmp.ui.screens.CalendarScreen
import com.hogargo.kmp.ui.screens.FinanceScreen
import com.hogargo.kmp.ui.screens.HomeScreen
import com.hogargo.kmp.ui.screens.PetsScreen
import com.hogargo.kmp.ui.screens.WelcomeScreen

/** Rutas de la app. La navegacion vive 100% en commonMain. */
object Routes {
    const val WELCOME = "welcome"
    const val HOME = "home"
    const val FINANCE = "finance"
    const val PETS = "pets"
    const val CALENDAR = "calendar"
    const val ABOUT = "about"
}

private data class TopLevel(val route: String, val label: String, val emoji: String)

private val topLevelDestinations = listOf(
    TopLevel(Routes.HOME, "Inicio", "🏠"),
    TopLevel(Routes.FINANCE, "Finanzas", "💰"),
    TopLevel(Routes.PETS, "Mascotas", "🐾"),
    TopLevel(Routes.CALENDAR, "Calendario", "📅"),
)

@Composable
fun AppNavHost(repository: HogarGoRepository) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                navController.navigate(dest.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Text(dest.emoji) },
                            label = { Text(dest.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.WELCOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.WELCOME) {
                WelcomeScreen(onStart = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                })
            }
            composable(Routes.HOME) {
                HomeScreen(repository, onOpenAbout = { navController.navigate(Routes.ABOUT) })
            }
            composable(Routes.FINANCE) { FinanceScreen(repository) }
            composable(Routes.PETS) { PetsScreen(repository) }
            composable(Routes.CALENDAR) { CalendarScreen(repository) }
            composable(Routes.ABOUT) { AboutScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
