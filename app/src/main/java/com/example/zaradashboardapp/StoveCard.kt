package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*

@Composable
fun StoveCard(
    stoveState: StoveState,
    onPowerToggle: (Boolean) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    // Stato locale per lo slider per evitare spam MQTT durante il trascinamento
    var localLevel by remember(stoveState.potenza) { mutableStateOf(stoveState.potenza.toFloat()) }

    DashboardCard(
        title = "Caminetto Palazzetti",
        accentColor = Color(0xFFFF5722) // Orange-Red fire color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Cerchio con Fiamma (Stato)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(if (stoveState.acceso) Color(0xFFFF5722) else Color.DarkGray)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (stoveState.acceso) "ACCESO" else "SPENTO",
                            color = if (stoveState.acceso) GreenActive else GreyText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "LIVELLO P${stoveState.potenza}",
                            color = OffWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Campo Modalità (non editabile)
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = stoveState.modalita.uppercase(),
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Controllo Potenza con Slider Discreto (Anti-Spam)
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Regola Potenza",
                        color = GreyText,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Imposta: P${localLevel.toInt()}",
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Slider(
                    value = localLevel,
                    onValueChange = { localLevel = it },
                    onValueChangeFinished = { onLevelChange(localLevel.toInt()) },
                    valueRange = 1f..6f,
                    steps = 4, // 1, (2, 3, 4, 5), 6
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFFFF5722),
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Pulsante Power (Unico pulsante rimasto, gli altri sostituiti dallo slider)
            Button(
                onClick = { onPowerToggle(!stoveState.acceso) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (stoveState.acceso) Color(0xFF424242) else Color(0xFFFF5722)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (stoveState.acceso) "SPEGNI CAMINETTO" else "ACCENDI CAMINETTO", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
