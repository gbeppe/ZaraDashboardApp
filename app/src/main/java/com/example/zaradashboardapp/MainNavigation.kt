package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.zaradashboardapp.ui.theme.*
import java.util.*

enum class AppRoute(val route: String) {
    HOME("home"),
    CLIMATE("climate"),
    ANALYTICS("analytics"),
    AI_ASSISTANT("ai_assistant"),
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
    BottomNavItem(AppRoute.AI_ASSISTANT, Icons.Default.AutoAwesome, "Zara AI"),
    BottomNavItem(AppRoute.SETTINGS, Icons.Default.Settings, "Setup")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    dashboardViewModel: DashboardViewModel,
    aiViewModel: AiAssistantViewModel
) {
    val navController = rememberNavController()
    val uiState by dashboardViewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    // Ticking Clock Simulation
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clockFormat = java.text.SimpleDateFormat("EEEE dd MMMM, HH:mm:ss", Locale.ITALIAN)
        while (true) {
            currentTimeString = clockFormat.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    if (showSettings) {
        SettingsDialog(
            currentSettings = dashboardViewModel.getSettings(),
            onDismiss = { showSettings = false },
            onSave = { newSettings ->
                dashboardViewModel.saveSettings(newSettings)
                showSettings = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "ZARA DASHBOARD",
                            color = TealPrimary,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = currentTimeString.uppercase(),
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                },
                actions = {
                    ConnectionIndicator(status = uiState.connectionStatus)
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    // System AI Master Status Switch - Compact Version
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (uiState.isGlobalEnabled) Color(0x1F00C853) else Color(0x1FFF1744))
                            .clickable { dashboardViewModel.toggleSystem() }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isGlobalEnabled) GreenActive else Color.Red)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (uiState.isGlobalEnabled) "AI" else "OFF",
                                color = if (uiState.isGlobalEnabled) GreenActive else Color.Red,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )
        },
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
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
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
                    onToggleLight = { name, state -> dashboardViewModel.setLightState(name, state) },
                    onSetLightingScene = { dashboardViewModel.setLightingScene(it) }
                )
            }
            composable(AppRoute.CLIMATE.route) {
                ClimateScreen(
                    uiState = uiState,
                    onSetClimateTemp = { dashboardViewModel.setClimateTarget(it) },
                    onSetVmcSpeed = { dashboardViewModel.setVmcSpeed(it) },
                    onUpdateControl = { name, value -> dashboardViewModel.updateControl(name, value) },
                    onStovePowerToggle = { dashboardViewModel.setStovePower(it) },
                    onStoveLevelChange = { dashboardViewModel.setStoveLevel(it) }
                )
            }
            composable(AppRoute.ANALYTICS.route) {
                AnalyticsScreen(uiState = uiState)
            }
            composable(AppRoute.AI_ASSISTANT.route) {
                GenerativeUiScreen(
                    aiViewModel = aiViewModel,
                    dashboardViewModel = dashboardViewModel
                )
            }
            composable(AppRoute.SETTINGS.route) {
                SettingsScreen(
                    viewModel = dashboardViewModel,
                    isHolidayMode = uiState.isHolidayMode,
                    onToggleHoliday = { dashboardViewModel.toggleHolidayMode() }
                )
            }
        }
    }
}

