package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import com.example.zaradashboardapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun DashboardPreview() {
    // Per il preview usiamo un mock dello stato senza passare per il ViewModel reale che richiede Application
    ZaraDashboardAppTheme(darkTheme = true) {
        Column(
            modifier = Modifier
                .background(DarkBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EnergyCard(energy = EnergyMetrics(productionFvW = 4500, consumptionHomeW = 1200, powerwallSoc = 85, surplusW = 3300))
            ClimateCard(climate = ClimateState(targetTemp = 22, isAcOn = true, reason = "Free Cooling"), onSetTemp = {})
            LightsCard(lightsMap = mapOf("sala" to true, "cucina" to false), onToggleLight = { _, _ -> })
            EnvironmentCard(env = EnvironmentalMetrics(tempLiving = 21.5f, humLiving = 45f, tempBedroom = 19.8f, humBedroom = 50f, tempOutdoor = 12.0f, humOutdoor = 65f))
            HeatingCard(heating = HeatingState(acsBufferTemp = 48.5f, highBufferTemp = 55.0f, lowBufferTemp = 32.0f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            currentSettings = viewModel.getSettings(),
            onDismiss = { showSettings = false },
            onSave = { newSettings ->
                viewModel.saveSettings(newSettings)
                showSettings = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Zara Dashboard",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    ConnectionIndicator(status = uiState.connectionStatus)
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Welcome Header
            Text(
                text = "Bentornato a Casa",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light
            )

            // Section: Header & Global Controls
            GlobalControlsSection(
                isSystemEnabled = uiState.isGlobalEnabled,
                isHolidayMode = uiState.isHolidayMode,
                onToggleSystem = { viewModel.toggleSystem() },
                onToggleHoliday = { viewModel.toggleHolidayMode() }
            )

            // Section: Energy
            EnergyCard(energy = uiState.energy)

            // Section: Climate
            ClimateCard(
                climate = uiState.climate,
                onSetTemp = { viewModel.setClimateTarget(it) }
            )

            // Section: VMC
            VmcCard(
                vmc = uiState.vmc,
                onSetSpeed = { viewModel.setVmcSpeed(it) }
            )

            // Section: Lights
            LightsCard(
                lightsMap = uiState.lights,
                onToggleLight = { name, state -> viewModel.setLightState(name, state) }
            )

            // Section: Environments
            EnvironmentCard(env = uiState.env)

            // Section: Heating
            HeatingCard(heating = uiState.heating)

            // Section: Rooms / Environments
            DashboardCard(title = "Ambienti", accentColor = Sunset) {
                Box(modifier = Modifier.height(100.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("Placeholder Ambienti", color = Color.Gray)
                }
            }

            // Section: Logs
            SystemLogsCard(logs = uiState.recentLogs)
        }
    }
}

@Composable
fun GlobalControlsSection(
    isSystemEnabled: Boolean,
    isHolidayMode: Boolean,
    onToggleSystem: () -> Unit,
    onToggleHoliday: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Master AI System
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = Emerald)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Cervello AI (Master)", color = Color.White, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = isSystemEnabled,
                    onCheckedChange = { onToggleSystem() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Emerald,
                        checkedTrackColor = Emerald.copy(alpha = 0.4f)
                    )
                )
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            // Holiday Mode
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.BeachAccess, contentDescription = null, tint = Sunset)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Modalità Vacanza (Antigelo)", color = Color.White, fontWeight = FontWeight.Medium)
                }
                Switch(
                    checked = isHolidayMode,
                    onCheckedChange = { onToggleHoliday() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Sunset,
                        checkedTrackColor = Sunset.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun EnergyCard(energy: EnergyMetrics) {
    DashboardCard(title = "Flussi Energetici", accentColor = Emerald) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Main Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                EnergyMetricItem(
                    label = "Fotovoltaico",
                    value = "${energy.productionFvW} W",
                    icon = Icons.Default.SolarPower,
                    iconColor = Amber,
                    modifier = Modifier.weight(1f)
                )
                EnergyMetricItem(
                    label = "Casa",
                    value = "${energy.consumptionHomeW} W",
                    icon = Icons.Default.Home,
                    iconColor = Color.White,
                    modifier = Modifier.weight(1f)
                )
                EnergyMetricItem(
                    label = "Batteria",
                    value = "${energy.powerwallSoc}%",
                    icon = Icons.Default.Bolt,
                    iconColor = if (energy.powerwallSoc > 20) Emerald else Crimson,
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)

            // Grid Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            )
            {
                // ... (codice precedente della card) ...

                val gridValue = energy.gridPowerW // Il valore già filtrato dalla deadband
                val isDrawingFromGrid = gridValue > 0 // (Adatta il > o < in base al segno che usi)

// Calcoliamo lo sbilancio interno (deficit)
                val deficit = energy.consumptionHomeW - energy.productionFvW

                if (gridValue == 0) {
                    // La rete è a zero, capiamo cosa sta facendo la batteria
                    if (deficit > 0) {
                        // La casa consuma più del solare: la batteria interviene
                        Text(
                            text = "🔋 Prelievo da Batteria: $deficit W",
                            color = Color(0xFF00E676), // Un Semantic Emerald/Verde Elettrico brillante
                            fontWeight = FontWeight.Bold
                        )
                    } else if (deficit < 0) {
                        // Il solare produce più della casa: stiamo caricando l'accumulo
                        Text(
                            text = "⚡ Ricarica Batteria: ${kotlin.math.abs(deficit)} W",
                            color = Color(0xFF29B6F6), // Un brillante Sky Blue
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Raro, ma possibile: consumo e produzione sono identici al watt
                        Text("Rete Bilanciata", color = Color.Gray)
                    }
                } else if (isDrawingFromGrid) {
                    // Stiamo prelevando dal contatore (Enel/Servizio Elettrico)
                    Text("🔻 Prelievo Rete: $gridValue W", color = Color(0xFFFF5252)) // Rosso
                } else {
                    // Stiamo immettendo in rete il surplus
                    Text("🌱 Immissione Rete: ${kotlin.math.abs(gridValue)} W", color = Color(0xFF81C784)) // Verde tenue
                }
            }
        }
    }
}

@Composable
fun ClimateCard(climate: ClimateState, onSetTemp: (Int) -> Unit) {
    DashboardCard(title = "Climatizzazione", accentColor = SkyBlue) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header Info: AC Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (climate.isAcOn) SkyBlue else Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (climate.isAcOn) "AC ON" else "AC OFF",
                    color = if (climate.isAcOn) SkyBlue else Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Main Controls: Temp and Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Temperature Controls
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${climate.targetTemp}°C",
                        color = Color.White,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onSetTemp(climate.targetTemp - 1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Sottrai", tint = Color.White)
                        }
                        IconButton(
                            onClick = { onSetTemp(climate.targetTemp + 1) },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = DarkSurfaceVariant)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Aggiungi", tint = Color.White)
                        }
                    }
                }

                // Right: Mode and Fan
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InfoRow(label = "MODO", value = climate.mode)
                    InfoRow(label = "FAN", value = climate.fanSpeed)
                }
            }

            // AI Footer
            if (climate.isAutoModeEnabled) {
                AiReasonFooter(reason = climate.reason)
            }
        }
    }
}

@Composable
fun VmcCard(vmc: VmcState, onSetSpeed: (Int) -> Unit) {
    DashboardCard(title = "Ventilazione (VMC)", accentColor = SkyBlue) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header Info: Bypass
            if (vmc.bypassOpen) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Emerald.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = IconButtonDefaults.outlinedIconButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Emerald))
                    ) {
                        Text(
                            text = "Bypass Aperto",
                            color = Emerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Main Content: Speed Selection
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Velocità attuale", color = Color.Gray, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (speed in 1..4) {
                        SpeedChip(
                            speed = speed,
                            isSelected = vmc.fanSpeed == speed,
                            onSelect = { onSetSpeed(speed) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // AI Footer
            if (vmc.isAutoModeEnabled) {
                AiReasonFooter(reason = vmc.reason)
            }
        }
    }
}

@Composable
fun EnergyMetricItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(28.dp))
        Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun EnvironmentCard(env: EnvironmentalMetrics) {
    DashboardCard(title = "Condizioni Ambientali", accentColor = Emerald) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            EnvironmentItem(label = "Salotto", icon = "🛋️", temp = env.tempLiving, hum = env.humLiving, modifier = Modifier.weight(1f))
            EnvironmentItem(label = "Notte", icon = "🛏️", temp = env.tempBedroom, hum = env.humBedroom, modifier = Modifier.weight(1f))
            EnvironmentItem(label = "Esterno", icon = "🌳", temp = env.tempOutdoor, hum = env.humOutdoor, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun EnvironmentItem(label: String, icon: String, temp: Float, hum: Float, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 20.sp)
        Text(label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text("${temp}°C", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("$hum%", color = Color.Gray, fontSize = 11.sp)
    }
}

@Composable
fun HeatingCard(heating: HeatingState) {
    DashboardCard(title = "Centrale Termica", accentColor = Sunset) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            HeatingRow(
                label = "Acqua Sanitaria (ACS)",
                temp = heating.acsBufferTemp,
                icon = Icons.Default.WaterDrop,
                accentColor = if (heating.acsBufferTemp > 45) Sunset else SkyBlue
            )
            HeatingRow(
                label = "Puffer Riscaldamento (Alto)",
                temp = heating.highBufferTemp,
                icon = Icons.Default.HotTub,
                accentColor = Sunset
            )
            HeatingRow(
                label = "Puffer Riscaldamento (Basso)",
                temp = heating.lowBufferTemp,
                icon = Icons.Default.Waves,
                accentColor = SkyBlue
            )
        }
    }
}

@Composable
fun HeatingRow(label: String, temp: Float, icon: androidx.compose.ui.graphics.vector.ImageVector, accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            text = "${temp}°C",
            color = accentColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun LightsCard(
    lightsMap: Map<String, Boolean>,
    onToggleLight: (String, Boolean) -> Unit
) {
    val defaultLights = listOf(
        "sala", "libreria", "cucina", "televisione",
        "portico", "esterno", "luciPiscina", "pompaPiscina"
    )

    DashboardCard(title = "Illuminazione & Relè", accentColor = Amber) {
        // Usiamo una griglia manuale per semplicità e controllo del layout dentro lo scroll
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val chunks = defaultLights.chunked(2)
            chunks.forEach { rowLights ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowLights.forEach { lightName ->
                        val isActive = lightsMap[lightName] ?: false
                        LightButton(
                            name = lightName,
                            isActive = isActive,
                            onClick = { onToggleLight(lightName, !isActive) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowLights.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun LightButton(
    name: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Amber.copy(alpha = 0.2f) else DarkSurfaceVariant,
        border = if (isActive) IconButtonDefaults.outlinedIconButtonBorder(enabled = true).copy(brush = androidx.compose.ui.graphics.SolidColor(Amber)) else null,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (isActive) Icons.Default.Lightbulb else Icons.Default.TipsAndUpdates,
                contentDescription = null,
                tint = if (isActive) Amber else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = name.replaceFirstChar { it.uppercase() },
                color = if (isActive) Color.White else Color.Gray,
                fontSize = 14.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun SpeedChip(speed: Int, isSelected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SkyBlue else DarkSurfaceVariant,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = speed.toString(),
                color = if (isSelected) Color.White else Color.Gray,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun AiReasonFooter(reason: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2A2A),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.SmartToy,
                contentDescription = null,
                tint = Amber,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = reason,
                color = Amber,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 16.dp)
                        .background(accentColor, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun SystemLogsCard(logs: List<LogEvent>) {
    DashboardCard(title = "Telemetria & Diario AI", accentColor = Amber) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun evento telemetrico", color = Color.Gray, fontSize = 12.sp)
                }
            } else {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    logs.forEach { log ->
                        AiLogRow(log)
                    }
                }
            }
        }
    }
}

@Composable
fun AiLogRow(log: LogEvent) {
    val accentColor = when (log.eventType) {
        "AI_ENGINE" -> if (log.message.contains("CAMBIO")) SkyBlue else Emerald
        "ACTION" -> Emerald
        "ERROR" -> Crimson
        else -> Amber
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Column
        Text(
            text = log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )

        // Content Column
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.message,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = log.details,
                color = accentColor.copy(alpha = 0.9f),
                fontSize = 12.sp,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                ),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ConnectionIndicator(status: ConnectionStatus) {
    val color = when (status) {
        ConnectionStatus.CONNECTED_LOCAL, ConnectionStatus.CONNECTED_REMOTE -> Emerald
        ConnectionStatus.CONNECTING -> Amber
        ConnectionStatus.DISCONNECTED -> Crimson
    }

    val icon = when (status) {
        ConnectionStatus.CONNECTED_LOCAL -> Icons.Default.Home
        ConnectionStatus.CONNECTED_REMOTE -> Icons.Default.Public
        ConnectionStatus.CONNECTING -> Icons.Default.Sync
        ConnectionStatus.DISCONNECTED -> Icons.Default.PowerOff
    }
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (status == ConnectionStatus.CONNECTING) {
            CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                color = color,
                strokeWidth = 2.dp
            )
        } else {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = when(status) {
                ConnectionStatus.CONNECTED_LOCAL -> "LOCALE"
                ConnectionStatus.CONNECTED_REMOTE -> "REMOTO"
                ConnectionStatus.CONNECTING -> "Connessione..."
                ConnectionStatus.DISCONNECTED -> "OFFLINE"
            },
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SettingsDialog(
    currentSettings: SettingsManager.MqttSettings,
    onDismiss: () -> Unit,
    onSave: (SettingsManager.MqttSettings) -> Unit
) {
    var localIp by remember { mutableStateOf(currentSettings.localIp) }
    var remoteIp by remember { mutableStateOf(currentSettings.remoteIp) }
    var port by remember { mutableStateOf(currentSettings.port) }
    var username by remember { mutableStateOf(currentSettings.username) }
    var password by remember { mutableStateOf(currentSettings.password) }
    var baseTopic by remember { mutableStateOf(currentSettings.baseTopic) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Configurazione MQTT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                SettingsField(label = "Local IP Broker", value = localIp, onValueChange = { localIp = it })
                SettingsField(label = "Remote IP (4G/5G)", value = remoteIp, onValueChange = { remoteIp = it })
                SettingsField(label = "Porta", value = port, onValueChange = { port = it })
                SettingsField(label = "Username", value = username, onValueChange = { username = it })
                SettingsField(label = "Password", value = password, onValueChange = { password = it }, isPassword = true)
                SettingsField(label = "Topic Base", value = baseTopic, onValueChange = { baseTopic = it })

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        onSave(SettingsManager.MqttSettings(localIp, remoteIp, port, username, password, baseTopic))
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("SALVA E RICONNETTI", color = DarkBackground, fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Annulla", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun SettingsField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedBorderColor = Emerald,
            unfocusedBorderColor = Color.DarkGray
        ),
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None
    )
}
