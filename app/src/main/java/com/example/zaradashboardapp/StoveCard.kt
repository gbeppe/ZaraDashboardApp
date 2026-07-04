package com.example.zaradashboardapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoveCard(
    stoveState: StoveState,
    onPowerToggle: (Boolean) -> Unit,
    onModeChange: (String) -> Unit,
    onLevelChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header (Icona + Titolo + Switch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Caminetto",
                        tint = if (stoveState.acceso) Color(0xFFE53935) else Color.Gray,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Caminetto Palazzetti",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Switch(
                    checked = stoveState.acceso,
                    onCheckedChange = { onPowerToggle(it) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modalità (Menu a tendina)
            val modes = listOf("sanitaria", "con caldaia", "riscaldamento", "manuale", "disattivato")
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = stoveState.modalita.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Modalità", color = Color.LightGray) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modes.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                onModeChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Potenza (Slider a scatti discreti)
            Text(
                text = "Livello Potenza: ${stoveState.potenza}",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Slider(
                value = stoveState.potenza.toFloat(),
                onValueChange = { onLevelChange(it.toInt()) },
                valueRange = 1f..6f,
                steps = 4, // Genera i blocchi interni 2, 3, 4, 5
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
