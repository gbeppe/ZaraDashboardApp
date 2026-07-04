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
fun ClimateScreen(
    uiState: SystemState,
    onSetClimateTemp: (Int) -> Unit,
    onSetVmcSpeed: (Int) -> Unit,
    onUpdateControl: (String, Any) -> Unit,
    onStovePowerToggle: (Boolean) -> Unit,
    onStoveModeChange: (String) -> Unit,
    onStoveLevelChange: (Int) -> Unit
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
        // Section: Climate
        ClimateCard(
            climate = uiState.climate,
            onSetTemp = onSetClimateTemp
        )

        // Section: VMC
        VmcCard(
            vmc = uiState.vmc,
            onSetSpeed = onSetVmcSpeed
        )

        // Section: Stove (Fireplace)
        StoveCard(
            stoveState = uiState.stove,
            onPowerToggle = onStovePowerToggle,
            onModeChange = onStoveModeChange,
            onLevelChange = onStoveLevelChange
        )

        // Section: Heating
        HeatingCard(heating = uiState.heating)

        // Section: Controls (Advanced)
        ControlsCard(
            controls = uiState.controls,
            onUpdate = onUpdateControl
        )
    }
}
