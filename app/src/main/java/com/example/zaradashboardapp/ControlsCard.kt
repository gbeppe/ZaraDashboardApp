package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*

@Composable
fun ControlsCard(
    controls: ControlsState,
    onUpdate: (String, Any) -> Unit
) {
    DashboardCard(title = "Controlli Parametri", accentColor = SkyBlue) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            
            // Stepper Rows
            ControlStepperRow(
                label = "Minuti ON Compressore",
                value = controls.minOnCompressore,
                onValueChange = { onUpdate("minOnCompressore", it) }
            )
            
            ControlStepperRow(
                label = "Minuti OFF Compressore",
                value = controls.minOffCompressore,
                onValueChange = { onUpdate("minOffCompressore", it) }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            ControlStepperRow(
                label = "Soglia Humidex Notte",
                value = controls.sogliaHumidexNotte,
                onValueChange = { onUpdate("sogliaHumidexNotte", it) }
            )

            FloatStepperRow(
                label = "Soglia Emergenza Humidex",
                value = controls.sogliaEmergenzaHumidex,
                step = 0.5f,
                onValueChange = { onUpdate("sogliaEmergenzaHumidex", it) }
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            ControlStepperRow(
                label = "Tolleranza Deficit (W)",
                value = controls.tolleranzaDeficit,
                onValueChange = { onUpdate("tolleranzaDeficit", it) }
            )

            // VMC Slider
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Velocità Max VMC Notte", color = OffWhite, fontSize = 13.sp)
                    Text("${controls.velocitaMaxVmcNotte.toInt()}", color = SkyBlue, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Slider(
                    value = controls.velocitaMaxVmcNotte,
                    onValueChange = { onUpdate("velocitaMaxVmcNotte", it) },
                    valueRange = 1f..4f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = SkyBlue,
                        activeTrackColor = SkyBlue,
                        inactiveTrackColor = DarkSurfaceVariant
                    )
                )
            }

            // AC Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Gestione AC Mattino", color = OffWhite, fontSize = 13.sp)
                    Text(
                        text = if (controls.gestioneAcMattinoSolar) "MODE: SOLAR_ONLY" else "MODE: AWAY",
                        color = if (controls.gestioneAcMattinoSolar) GreenActive else GreyText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Switch(
                    checked = controls.gestioneAcMattinoSolar,
                    onCheckedChange = { onUpdate("gestioneAcMattinoSolar", it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = GreenActive,
                        checkedTrackColor = GreenActive.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
fun ControlStepperRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = OffWhite, fontSize = 13.sp)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { onValueChange(value - 1) },
                modifier = Modifier.size(32.dp).background(DarkSurfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            
            Text(
                text = value.toString(),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 24.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(32.dp).background(DarkSurfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun FloatStepperRow(label: String, value: Float, step: Float, onValueChange: (Float) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Text(text = label, color = OffWhite, fontSize = 13.sp, modifier = Modifier.weight(1f))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { onValueChange(value - step) },
                modifier = Modifier.size(32.dp).background(DarkSurfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Meno", tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Text(
                text = String.format(java.util.Locale.US, "%.1f", value),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { onValueChange(value + step) },
                modifier = Modifier.size(32.dp).background(DarkSurfaceVariant, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Più", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}
