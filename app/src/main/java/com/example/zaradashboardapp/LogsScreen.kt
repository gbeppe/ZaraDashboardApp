package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.zaradashboardapp.ui.theme.DarkBackground
import com.example.zaradashboardapp.ui.theme.TealPrimary

@Composable
fun LogsScreen(uiState: SystemState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, androidx.compose.ui.graphics.Color(0xFF15181C))
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Registro Eventi Completo",
            style = MaterialTheme.typography.headlineSmall,
            color = TealPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // In una schermata dedicata diamo tutto lo spazio ai log
        SystemLogsCard(
            logs = uiState.recentLogs,
            modifier = Modifier.weight(1f)
        )
    }
}
