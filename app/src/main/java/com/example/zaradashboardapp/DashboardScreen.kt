package com.example.zaradashboardapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.format.DateTimeFormatter
import com.example.zaradashboardapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var showSettings by remember { mutableStateOf(false) }

    // Ticking Clock Simulation
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val clockFormat = java.text.SimpleDateFormat("EEEE dd MMMM, HH:mm:ss", java.util.Locale.ITALIAN)
        while (true) {
            currentTimeString = clockFormat.format(java.util.Date())
            kotlinx.coroutines.delay(1000)
        }
    }

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
                            .clickable { viewModel.toggleSystem() }
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
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBackground, Color(0xFF15181C))
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

            // Section: Heating
            HeatingCard(heating = uiState.heating)

            // Section: Lights
            LightsCard(
                lightsMap = uiState.lights,
                onToggleLight = { name, state -> viewModel.setLightState(name, state) }
            )

            // Section: Automatismi
            DashboardCard(title = "Automatismi & Settaggi", accentColor = TealPrimary) {
                AutomationSwitchRow(
                    label = "Modalità Vacanza (Antigelo)",
                    checked = uiState.isHolidayMode,
                    onCheckedChange = { viewModel.toggleHolidayMode() }
                )
            }

            // Section: Logs
            SystemLogsCard(logs = uiState.recentLogs)
        }
    }
}

@Composable
fun EnergyCard(energy: EnergyMetrics) {
    DashboardCard(title = "Metering Energetico", accentColor = TealPrimary) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Gauges Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PowerGauge(
                    value = energy.consumptionHomeW,
                    maxLimit = 5000,
                    title = "CONSU-CASA",
                    unit = "W",
                    colors = listOf(GreenActive, Amber, Crimson),
                    modifier = Modifier.weight(1f)
                )
                PowerGauge(
                    value = energy.productionFvW,
                    maxLimit = 5000,
                    title = "SOLARE FV",
                    unit = "W",
                    colors = listOf(SkyBlue, Amber, OrangeAccent),
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // Battery Section (Tesla Style)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔋", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("TESLA POWERWALL", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Stato Ricarica Accumulatore", color = GreyText, fontSize = 11.sp)
                        }
                    }
                    Text(
                        text = "${energy.powerwallSoc}%",
                        color = if (energy.powerwallSoc > 20) GreenActive else Crimson,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }

                // Battery linear indicator bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(energy.powerwallSoc / 100f)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Crimson, Amber, GreenActive)
                                )
                            )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val surplus = energy.productionFvW - energy.consumptionHomeW
                    Text(
                        text = "Surplus Solare: $surplus W",
                        color = if (surplus >= 0) GreenActive else OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (surplus >= 0) "IN CARICA" else "SCARICA (RETE)",
                        color = if (surplus >= 0) GreenActive else OrangeAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            // Grid Balance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val gridValue = energy.gridPowerW
                val isDrawingFromGrid = gridValue > 0
                val deficit = energy.consumptionHomeW - energy.productionFvW

                if (gridValue == 0) {
                    if (deficit > 0) {
                        Text("🔋 Prelievo da Batteria: $deficit W", color = GreenActive, fontWeight = FontWeight.Bold)
                    } else if (deficit < 0) {
                        Text("⚡ Ricarica Batteria: ${kotlin.math.abs(deficit)} W", color = BlueCool, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Rete Bilanciata", color = GreyText)
                    }
                } else if (isDrawingFromGrid) {
                    Text("🔻 Prelievo Rete: $gridValue W", color = Crimson, fontWeight = FontWeight.Bold)
                } else {
                    Text("🌱 Immissione Rete: ${kotlin.math.abs(gridValue)} W", color = GreenActive, fontWeight = FontWeight.Bold)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TERMOSTATO LIVING",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TealPrimary,
                    letterSpacing = 0.5.sp
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(
                            onClick = { onSetTemp(climate.targetTemp - 1) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(DarkSurfaceVariant, CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Sottrai", tint = Color.White)
                        }
                        IconButton(
                            onClick = { onSetTemp(climate.targetTemp + 1) },
                            modifier = Modifier
                                .size(40.dp)
                                .background(DarkSurfaceVariant, CircleShape)
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
                    InfoRow(label = "AUTO", value = if (climate.isAutoModeEnabled) "ON" else "OFF")
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
    DashboardCard(title = "Ventilazione (VMC)", accentColor = TealPrimary) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealSecondary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🌀", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AI CLIMATE MANAGEMENT ENGINE",
                        color = TealPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Stato: ${vmc.reason}",
                        color = OffWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }

            Text(
                text = "REGOLAZIONE MANUALE VELOCITÀ",
                color = GreyText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (speed in 1..4) {
                    val isSelected = vmc.fanSpeed == speed
                    Button(
                        onClick = { onSetSpeed(speed) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) TealPrimary else DarkSurfaceVariant,
                            contentColor = if (isSelected) DarkBackground else OffWhite
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Text("$speed", fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            if (vmc.bypassOpen) {
                Surface(
                    color = GreenActive.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GreenActive.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "Bypass Aperto (Free Cooling)",
                        color = GreenActive,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun EnvironmentCard(env: EnvironmentalMetrics) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "CONDIZIONI AMBIENTALI",
            color = TealPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickMetricCard(
                title = "SALOTTO",
                temp = "${"%.1f".format(env.tempLiving)}°C",
                hum = "${env.humLiving.toInt()}% UR",
                status = "Humidex: ${"%.1f".format(env.humidexLiving)}",
                modifier = Modifier.weight(1f),
                color = TealPrimary
            )
            QuickMetricCard(
                title = "NOTTE",
                temp = "${"%.1f".format(env.tempBedroom)}°C",
                hum = "${env.humBedroom.toInt()}% UR",
                status = "Humidex: ${"%.1f".format(env.humidexBedroom)}",
                modifier = Modifier.weight(1f),
                color = BlueCool
            )
            QuickMetricCard(
                title = "ESTERNO",
                temp = "${"%.1f".format(env.tempOutdoor)}°C",
                hum = "${env.humOutdoor.toInt()}% UR",
                status = "",
                modifier = Modifier.weight(1f),
                color = OrangeAccent
            )
        }
    }
}

@Composable
fun HeatingCard(heating: HeatingState) {
    DashboardCard(title = "Centrale Termica", accentColor = Sunset) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PufferSondeItem(label = "Puffer ACS", value = "${"%.1f".format(heating.acsBufferTemp)}°C", color = OrangeAccent, modifier = Modifier.weight(1f))
                PufferSondeItem(label = "Puffer Alto", value = "${"%.1f".format(heating.highBufferTemp)}°C", color = TealPrimary, modifier = Modifier.weight(1f))
                PufferSondeItem(label = "Puffer Basso", value = "${"%.1f".format(heating.lowBufferTemp)}°C", color = BlueCool, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun LightsCard(
    lightsMap: Map<String, Boolean>,
    onToggleLight: (String, Boolean) -> Unit
) {
    val lightList = listOf(
        Triple("sala", "Sala", "🛋️"),
        Triple("libreria", "Libreria", "📚"),
        Triple("televisione", "Televisore", "📺"),
        Triple("portico", "Portico", "🏡"),
        Triple("cucina", "Cucina", "🍳"),
        Triple("esterno", "Esterno", "🌳"),
        Triple("luciPiscina", "Luci Piscina", "🏊"),
        Triple("pompaPiscina", "Pompa Filtro", "🌀")
    )

    DashboardCard(title = "Illuminazione & Relè", accentColor = Amber) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            lightList.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { (id, label, emoji) ->
                        val isActive = lightsMap[id] ?: false
                        LightToggleCard(
                            label = label,
                            emoji = emoji,
                            isActive = isActive,
                            onToggle = { onToggleLight(id, !isActive) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = GreyText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
fun QuickMetricCard(
    title: String,
    temp: String,
    hum: String,
    status: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GreyText,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = temp,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = hum,
                fontSize = 10.sp,
                color = OffWhite.copy(alpha = 0.7f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = status,
                fontSize = 10.sp,
                color = color.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LightToggleCard(
    label: String,
    emoji: String,
    isActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onToggle,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) TealSecondary.copy(alpha = 0.15f) else DarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) TealPrimary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.06f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else OffWhite.copy(alpha = 0.8f)
                )
                Text(
                    text = if (isActive) "ACCESO" else "SPENTO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) TealPrimary else GreyText
                )
            }
            // Tiny status indicator
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) TealPrimary else Color.Transparent)
                    .border(1.dp, if (isActive) TealPrimary else GreyText.copy(alpha = 0.4f), CircleShape)
            )
        }
    }
}

@Composable
fun PufferSondeItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurfaceVariant)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GreyText)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@Composable
fun AutomationSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OffWhite, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TealPrimary,
                checkedTrackColor = TealSecondary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
fun DashboardCard(
    title: String,
    accentColor: Color = TealPrimary,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            color = accentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SystemLogsCard(logs: List<LogEvent>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "LOG EVENTI ENGINE CLIMA AI & DOMOTICA",
            color = TealPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF070707))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun log registrato.", color = GreyText, fontSize = 12.sp)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    logs.forEach { log ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (log.isSystemEnabled) Color(0x05FFFFFF) else Color(0x0FFF1744))
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "[${log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}]",
                                    color = TealPrimary,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.eventType,
                                    color = if (log.isSystemEnabled) OrangeAccent else Color.Red,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.message,
                                color = OffWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(1.dp))
                            Text(
                                text = log.details,
                                color = GreyText,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
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
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
                    ConnectionStatus.CONNECTING -> "..."
                    ConnectionStatus.DISCONNECTED -> "OFF"
                },
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
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
fun PowerGauge(
    value: Int,
    maxLimit: Int,
    title: String,
    unit: String,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "GaugeAnimation"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.4f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val centerX = size.width / 2
                val centerY = size.height / 0.8f // Sposta il centro in basso per un arco più ampio
                val radius = size.width * 0.45f
                
                // Background Arc
                drawArc(
                    color = Color.White.copy(alpha = 0.05f),
                    startAngle = 160f,
                    sweepAngle = 220f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // Foreground Arc
                drawArc(
                    brush = Brush.horizontalGradient(colors),
                    startAngle = 160f,
                    sweepAngle = (animatedValue / maxLimit).coerceIn(0f, 1f) * 220f,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(centerX - radius, centerY - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )

                // Needle
                val needleAngle = 160f + (animatedValue / maxLimit).coerceIn(0f, 1f) * 220f
                val angleRad = Math.toRadians(needleAngle.toDouble())
                val needleLen = radius * 0.9f
                
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(centerX, centerY),
                    end = androidx.compose.ui.geometry.Offset(
                        (centerX + kotlin.math.cos(angleRad) * needleLen).toFloat(),
                        (centerY + kotlin.math.sin(angleRad) * needleLen).toFloat()
                    ),
                    strokeWidth = 3.dp.toPx(),
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                )
            }
            
            Column(
                modifier = Modifier.align(Alignment.Center).padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$value",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = unit,
                    color = GreyText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text = title,
            color = GreyText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun DashboardPreview() {
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
