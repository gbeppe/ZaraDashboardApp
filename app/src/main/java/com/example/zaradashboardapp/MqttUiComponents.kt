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
