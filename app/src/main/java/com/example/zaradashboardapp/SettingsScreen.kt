package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.zaradashboardapp.ui.theme.DarkBackground
import com.example.zaradashboardapp.ui.theme.TealPrimary

@Composable
fun SettingsScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()

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
        DashboardCard(title = "Settaggi & Automatismi", accentColor = TealPrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AutomationSwitchRow(
                    label = "Casa modalità vacanza",
                    checked = uiState.isHolidayMode,
                    onCheckedChange = { viewModel.toggleHolidayMode() }
                )
                
                HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))

                AutomationSwitchRow(
                    label = "Accensione luci ECO",
                    checked = uiState.isLuciEcoEnabled,
                    onCheckedChange = { viewModel.toggleLuciEco() }
                )

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))

                AutomationSwitchRow(
                    label = "Luci Piscina AUTO",
                    checked = uiState.isLuciPiscinaAutoEnabled,
                    onCheckedChange = { viewModel.toggleLuciPiscinaAuto() }
                )

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))

                AutomationSwitchRow(
                    label = "Sensore Portico",
                    checked = uiState.isSensorePorticoEnabled,
                    onCheckedChange = { viewModel.toggleSensorePortico() }
                )

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))

                AutomationSwitchRow(
                    label = "AC Auto",
                    checked = uiState.climate.isAutoModeEnabled,
                    onCheckedChange = { viewModel.toggleAcAuto() }
                )

                HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.05f))

                TimeRangeWidget(
                    label = "Range Orario Operativo",
                    startHour = uiState.timeRangeStart,
                    endHour = uiState.timeRangeEnd,
                    onRangeChange = { start, end -> viewModel.setTimeRange(start, end) }
                )
            }
        }
    }
}
