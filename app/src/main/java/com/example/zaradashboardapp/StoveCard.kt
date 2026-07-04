package com.example.zaradashboardapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoveCard(
    stoveState: StoveState,
    onPowerToggle: (Boolean) -> Unit,
    onModeChange: (String) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val modes = listOf("sanitaria", "con caldaia", "riscaldamento", "manuale", "disattivato")

    DashboardCard(
        title = "Caminetto Palazzetti",
        accentColor = Color(0xFFFF5722) // Colore fiamma
    ) {
        Column(
            modifier = Modifier.padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Riga Intestazione e Stato
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (stoveState.acceso) Color(0xFFFF5722) else GreyText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (stoveState.acceso) "ACCESO" else "SPENTO",
                        color = if (stoveState.acceso) GreenActive else GreyText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "P${stoveState.potenza}",
                        color = OffWhite,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            // Selezione Modalità (Dropdown)
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = stoveState.modalita.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modalità", color = GreyText, fontSize = 12.sp) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = OffWhite,
                        unfocusedTextColor = OffWhite,
                        focusedBorderColor = Color(0xFFFF5722),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modes.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onModeChange(mode)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Pulsanti di Controllo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pulsante Power
                Button(
                    onClick = { onPowerToggle(!stoveState.acceso) },
                    modifier = Modifier.weight(1.2f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (stoveState.acceso) Color(0xFF424242) else Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (stoveState.acceso) "SPEGNI" else "ACCENDI", fontSize = 12.sp)
                }

                // Pulsante Meno Potenza
                FilledIconButton(
                    onClick = { if (stoveState.potenza > 1) onLevelChange(stoveState.potenza - 1) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF424242)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Diminuisci")
                }

                // Pulsante Più Potenza
                FilledIconButton(
                    onClick = { if (stoveState.potenza < 6) onLevelChange(stoveState.potenza + 1) },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF424242)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumenta")
                }
            }
        }
    }
}
