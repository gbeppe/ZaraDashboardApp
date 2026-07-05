package com.example.zaradashboardapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

@Composable
fun GenerativeUiScreen(viewModel: AiAssistantViewModel) {
    val uiJsonState by viewModel.uiJsonState.collectAsState()
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Area di visualizzazione dinamica
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            DynamicWidget(jsonString = uiJsonState)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Area di input comandi
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Es: Metti la VMC al massimo") },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendCommand(inputText)
                        inputText = ""
                    }
                },
                enabled = inputText.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia comando")
            }
        }
    }
}

@Composable
fun DynamicWidget(jsonString: String) {
    if (jsonString.isBlank()) {
        Text(
            text = "In attesa di comandi...",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
        return
    }

    val parsedData = remember(jsonString) {
        try {
            val json = JSONObject(jsonString)
            val uiType = json.optString("ui_type")
            Result.success(uiType to json)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    if (parsedData.isSuccess) {
        val (uiType, json) = parsedData.getOrThrow()
        when (uiType) {
            "vmc_card" -> VmcCardWidget(json)
            "temperature_card" -> TemperatureCardWidget(json)
            else -> FallbackCardWidget(jsonString)
        }
    } else {
        val exception = parsedData.exceptionOrNull()
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Errore nel parsing del JSON", fontWeight = FontWeight.Bold)
                Text(exception?.message ?: "Errore sconosciuto", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun VmcCardWidget(json: JSONObject) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Air, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stato VMC", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Velocità: ${json.optInt("speed")}", style = MaterialTheme.typography.bodyLarge)
            Text("Stato: ${json.optString("status")}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TemperatureCardWidget(json: JSONObject) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color(0xFFFF5722))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sensori Ambientali", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Soggiorno: ${json.optDouble("living", 0.0)}°C")
            Text("Camera: ${json.optDouble("bedroom", 0.0)}°C")
            Text("Esterno: ${json.optDouble("outdoor", 0.0)}°C")
        }
    }
}

@Composable
fun FallbackCardWidget(jsonString: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Widget Sconosciuto", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(jsonString, style = MaterialTheme.typography.bodySmall)
        }
    }
}
