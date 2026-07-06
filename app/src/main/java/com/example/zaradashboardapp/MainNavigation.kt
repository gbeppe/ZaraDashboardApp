package com.example.zaradashboardapp

import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

enum class AppMode {
    DASHBOARD, AI
}

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

val dashboardNavItems = listOf(
    BottomNavItem(AppRoute.HOME, Icons.Default.Home, "Home"),
    BottomNavItem(AppRoute.CLIMATE, Icons.Default.Thermostat, "Clima"),
    BottomNavItem(AppRoute.ANALYTICS, Icons.Default.BarChart, "Dati"),
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
    var appMode by remember { mutableStateOf(AppMode.DASHBOARD) }
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
            Column(modifier = Modifier.background(DarkBackground)) {
                // Main Header with Mode Selector
                CenterAlignedTopAppBar(
                    title = {
                        ModeSelector(
                            currentMode = appMode,
                            onModeSelected = { appMode = it }
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }) {
                            Icon(Icons.Default.Tune, contentDescription = "Config", tint = GreyText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
                
                // Secondary Info Row (Clock and Connection)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentTimeString.uppercase(),
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConnectionIndicator(status = uiState.connectionStatus)
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        // Interactive AI System Status Toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (uiState.isGlobalEnabled) GreenActive.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f))
                                .clickable { dashboardViewModel.toggleSystem() }
                                .border(
                                    1.dp, 
                                    if (uiState.isGlobalEnabled) GreenActive.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.isGlobalEnabled) GreenActive else Color.Red)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isGlobalEnabled) "AUTO" else "MANU",
                                    color = if (uiState.isGlobalEnabled) GreenActive else Color.Red,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            AnimatedVisibility(
                visible = appMode == AppMode.DASHBOARD,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                NavigationBar(
                    containerColor = DarkBackground,
                    contentColor = TealPrimary,
                    tonalElevation = 0.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    dashboardNavItems.forEach { item ->
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
                                unselectedIconColor = Color.Gray.copy(alpha = 0.6f),
                                unselectedTextColor = Color.Gray.copy(alpha = 0.6f),
                                indicatorColor = TealPrimary.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (appMode == AppMode.DASHBOARD) {
                NavHost(
                    navController = navController,
                    startDestination = AppRoute.HOME.route,
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
                    composable(AppRoute.SETTINGS.route) {
                        SettingsScreen(viewModel = dashboardViewModel)
                    }
                }
            } else {
                // AI Fullscreen Experience
                GenerativeUiScreen(
                    aiViewModel = aiViewModel,
                    dashboardViewModel = dashboardViewModel
                )
            }
        }
    }
}

@Composable
fun ModeSelector(
    currentMode: AppMode,
    onModeSelected: (AppMode) -> Unit
) {
    val indicatorOffset by animateDpAsState(
        targetValue = if (currentMode == AppMode.DASHBOARD) 0.dp else 100.dp,
        animationSpec = spring(stiffness = 500f),
        label = "ModeIndicator"
    )

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(2.dp)
    ) {
        // Sliding Background Indicator
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(98.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(TealPrimary.copy(alpha = 0.15f))
        )

        Row(modifier = Modifier.fillMaxSize()) {
            ModeButton(
                label = "DASHBOARD",
                isSelected = currentMode == AppMode.DASHBOARD,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(AppMode.DASHBOARD) }
            )
            ModeButton(
                label = "ZARA AI",
                isSelected = currentMode == AppMode.AI,
                modifier = Modifier.weight(1f),
                onClick = { onModeSelected(AppMode.AI) }
            )
        }
    }
}

@Composable
fun RowScope.ModeButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) TealPrimary else GreyText,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
    }
}

