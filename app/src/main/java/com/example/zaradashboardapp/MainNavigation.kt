package com.example.zaradashboardapp

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zaradashboardapp.ui.theme.DarkBackground
import com.example.zaradashboardapp.ui.theme.TealPrimary

enum class AppRoute(val route: String) {
    HOME("home"),
    CLIMATE("climate"),
    ANALYTICS("analytics"),
    SETTINGS("settings")
}

data class BottomNavItem(
    val route: AppRoute,
    val icon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(AppRoute.HOME, Icons.Default.Home, "Home"),
    BottomNavItem(AppRoute.CLIMATE, Icons.Default.Thermostat, "Clima"),
    BottomNavItem(AppRoute.ANALYTICS, Icons.Default.BarChart, "Dati"),
    BottomNavItem(AppRoute.SETTINGS, Icons.Default.Settings, "Setup")
)

@Composable
fun MainScreen(viewModel: DashboardViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkBackground,
                contentColor = TealPrimary
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route.route } == true,
                        onClick = {
                            navController.navigate(item.route.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TealPrimary,
                            selectedTextColor = TealPrimary,
                            unselectedIconColor = androidx.compose.ui.graphics.Color.Gray,
                            unselectedTextColor = androidx.compose.ui.graphics.Color.Gray,
                            indicatorColor = TealPrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoute.HOME.route) {
                HomeScreen(
                    uiState = uiState,
                    onToggleLight = { name, state -> viewModel.setLightState(name, state) }
                )
            }
            composable(AppRoute.CLIMATE.route) {
                ClimateScreen(
                    uiState = uiState,
                    onSetClimateTemp = { viewModel.setClimateTarget(it) },
                    onSetVmcSpeed = { viewModel.setVmcSpeed(it) },
                    onUpdateControl = { name, value -> viewModel.updateControl(name, value) }
                )
            }
            composable(AppRoute.ANALYTICS.route) {
                AnalyticsScreen()
            }
            composable(AppRoute.SETTINGS.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    isHolidayMode = uiState.isHolidayMode,
                    onToggleHoliday = { viewModel.toggleHolidayMode() }
                )
            }
        }
    }
}
