package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.DarkBackground
import com.example.zaradashboardapp.ui.theme.Emerald
import com.example.zaradashboardapp.ui.theme.TealPrimary

@Composable
fun SettingsScreen(
    viewModel: DashboardViewModel,
    isHolidayMode: Boolean,
    onToggleHoliday: (Boolean) -> Unit
) {
    val currentSettings = viewModel.getSettings()
    var localIp by remember { mutableStateOf(currentSettings.localIp) }
    var remoteIp by remember { mutableStateOf(currentSettings.remoteIp) }
    var port by remember { mutableStateOf(currentSettings.port) }
    var username by remember { mutableStateOf(currentSettings.username) }
    var password by remember { mutableStateOf(currentSettings.password) }
    var baseTopic by remember { mutableStateOf(currentSettings.baseTopic) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DashboardCard(title = "Automatismi", accentColor = TealPrimary) {
            AutomationSwitchRow(
                label = "Modalità Vacanza (Antigelo)",
                checked = isHolidayMode,
                onCheckedChange = onToggleHoliday
            )
        }

        DashboardCard(title = "Configurazione MQTT", accentColor = Emerald) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SettingsField(label = "Local IP Broker", value = localIp, onValueChange = { localIp = it })
                SettingsField(label = "Remote IP (4G/5G)", value = remoteIp, onValueChange = { remoteIp = it })
                SettingsField(label = "Porta", value = port, onValueChange = { port = it })
                SettingsField(label = "Username", value = username, onValueChange = { username = it })
                SettingsField(label = "Password", value = password, onValueChange = { password = it }, isPassword = true)
                SettingsField(label = "Topic Base", value = baseTopic, onValueChange = { baseTopic = it })

                Button(
                    onClick = {
                        viewModel.saveSettings(SettingsManager.MqttSettings(localIp, remoteIp, port, username, password, baseTopic))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SALVA E RICONNETTI", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
