package com.example.zaradashboardapp

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zaradashboardapp.ui.theme.*

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
                    ConnectionStatus.CONNECTED_LOCAL -> "WIFI"
                    ConnectionStatus.CONNECTED_REMOTE -> "RETE"
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
    tinyCamSettings: SettingsManager.TinyCamSettings,
    onDismiss: () -> Unit,
    onSaveMqtt: (SettingsManager.MqttSettings) -> Unit,
    onSaveTinyCam: (SettingsManager.TinyCamSettings) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("MQTT", "tinyCam")

    // MQTT States
    var localIp by remember { mutableStateOf(currentSettings.localIp) }
    var remoteIp by remember { mutableStateOf(currentSettings.remoteIp) }
    var port by remember { mutableStateOf(currentSettings.port) }
    var username by remember { mutableStateOf(currentSettings.username) }
    var password by remember { mutableStateOf(currentSettings.password) }
    var baseTopic by remember { mutableStateOf(currentSettings.baseTopic) }

    // tinyCam States
    var tcIp by remember { mutableStateOf(tinyCamSettings.ip) }
    var tcRemoteIp by remember { mutableStateOf(tinyCamSettings.remoteIp) }
    var tcPort by remember { mutableStateOf(tinyCamSettings.port) }
    var tcUser by remember { mutableStateOf(tinyCamSettings.user) }
    var tcPass by remember { mutableStateOf(tinyCamSettings.pass) }

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
                    .heightIn(max = 500.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Configurazione",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = TealPrimary,
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (selectedTabIndex == 0) {
                        SettingsField(label = "Local IP Broker", value = localIp, onValueChange = { localIp = it })
                        SettingsField(label = "Remote IP (4G/5G)", value = remoteIp, onValueChange = { remoteIp = it })
                        SettingsField(label = "Porta", value = port, onValueChange = { port = it })
                        SettingsField(label = "Username", value = username, onValueChange = { username = it })
                        SettingsField(label = "Password", value = password, onValueChange = { password = it }, isPassword = true)
                        SettingsField(label = "Topic Base", value = baseTopic, onValueChange = { baseTopic = it })
                    } else {
                        SettingsField(label = "Local IP tinyCam (WiFi)", value = tcIp, onValueChange = { tcIp = it })
                        SettingsField(label = "Remote IP tinyCam (Cellulare)", value = tcRemoteIp, onValueChange = { tcRemoteIp = it })
                        SettingsField(label = "Porta Web Server", value = tcPort, onValueChange = { tcPort = it })
                        SettingsField(label = "Username", value = tcUser, onValueChange = { tcUser = it })
                        SettingsField(label = "Password", value = tcPass, onValueChange = { tcPass = it }, isPassword = true)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (selectedTabIndex == 0) {
                            onSaveMqtt(SettingsManager.MqttSettings(localIp, remoteIp, port, username, password, baseTopic))
                        } else {
                            onSaveTinyCam(SettingsManager.TinyCamSettings(tcIp, tcRemoteIp, tcPort, tcUser, tcPass))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    val btnText = if (selectedTabIndex == 0) "SALVA E RICONNETTI MQTT" else "SALVA CONFIG TINYCAM"
                    Text(btnText, color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Chiudi", color = Color.Gray)
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
fun TimeRangeWidget(
    label: String,
    startHour: Int,
    endHour: Int,
    onRangeChange: (Int, Int) -> Unit
) {
    var range by remember(startHour, endHour) { 
        mutableStateOf(startHour.toFloat()..endHour.toFloat()) 
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = OffWhite, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Surface(
                color = TealPrimary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${range.start.toInt()}:00 - ${range.endInclusive.toInt()}:00",
                    color = TealPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        RangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = 0f..24f,
            steps = 23,
            onValueChangeFinished = {
                onRangeChange(range.start.toInt(), range.endInclusive.toInt())
            },
            colors = SliderDefaults.colors(
                thumbColor = TealPrimary,
                activeTrackColor = TealPrimary,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f),
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}
