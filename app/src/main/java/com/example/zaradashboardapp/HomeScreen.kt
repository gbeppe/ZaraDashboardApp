package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.zaradashboardapp.ui.theme.DarkBackground

@Composable
fun HomeScreen(
    uiState: SystemState,
    onToggleLight: (String, Boolean) -> Unit,
    onSetLightingScene: (String) -> Unit,
    onToggleHoliday: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, androidx.compose.ui.graphics.Color(0xFF15181C))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Section: Environments Quick Metrics
        EnvironmentCard(env = uiState.env)

        // Section: Energy
        EnergyCard(energy = uiState.energy)

        // Section: Lights
        LightsCard(
            lightsMap = uiState.lights,
            onToggleLight = onToggleLight,
            onSetScene = onSetLightingScene
        )

        // Section: Automatismi
        DashboardCard(title = "Automatismi", accentColor = com.example.zaradashboardapp.ui.theme.TealPrimary) {
            AutomationSwitchRow(
                label = "Modalità Vacanza (Antigelo)",
                checked = uiState.isHolidayMode,
                onCheckedChange = { onToggleHoliday() }
            )
        }

        // Section: Logs
        SystemLogsCard(logs = uiState.recentLogs)
    }
}
