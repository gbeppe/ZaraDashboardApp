package com.example.zaradashboardapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun OverviewScreen(uiState: SystemState) {
    val waiting = uiState.waitingForData
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DarkBackground, Color(0xFF15181C))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (waiting) {
                Text(
                    text = "⏳ Attesa valori…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Amber
                )
            } else {
                Text(
                    text = "Stagione attiva: ${uiState.activeSeason}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TealPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 1. Energia
        DashboardCard(title = "Energia", accentColor = Amber) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewRow("Produzione FV", "${"%.1f".format(uiState.energy.productionFvW.toFloat())} W")
                OverviewRow("Consumo Casa", "${"%.1f".format(uiState.energy.consumptionHomeW.toFloat())} W")
                
                val surplus = uiState.energy.surplusW
                OverviewRow(
                    label = "Surplus Solare",
                    value = "${"%.1f".format(surplus.toFloat())} W",
                    valueColor = if (surplus > 0) GreenActive else Crimson
                )
                
                OverviewRow("Tesla Powerwall", "${uiState.energy.powerwallSoc}%")

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.White.copy(alpha = 0.05f))

                // Solar Forecast Logic
                val forecastLabel = getForecastLabel(uiState.logic.previsioneSolareData)
                OverviewRow("Previsione Solare $forecastLabel", "${"%.1f".format(uiState.logic.previsioneSolareKwh)} kWh")
                OverviewRow(
                    label = "Stima Batteria $forecastLabel", 
                    value = "${"%.0f".format(uiState.logic.previsioneBatteriaPercent)}% (${"%.1f".format(uiState.logic.kwhStimatiBatteria)} kWh)"
                )
            }
        }

        // 2. Ambiente
        val roomLabel = if (uiState.logic.stanzaVmc == "BEDROOM") "Camera" else "Soggiorno"
        val roomTemp = if (uiState.logic.stanzaVmc == "BEDROOM") uiState.env.tempBedroom else uiState.env.tempLiving
        val roomHumidex = if (uiState.logic.stanzaVmc == "BEDROOM") uiState.env.humidexBedroom else uiState.env.humidexLiving

        DashboardCard(title = "Ambiente ($roomLabel)", accentColor = TealPrimary) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewRow("Temperatura", "${"%.1f".format(roomTemp)} °C")
                OverviewRow("Humidex", "%.1f".format(roomHumidex))
            }
        }

        // 3. Condizionatore
        DashboardCard(title = "Condizionatore", accentColor = BlueCool) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val climateStatus = if (uiState.climate.reason == "N/A") "OFF" else uiState.climate.reason
                OverviewRow("Stato", climateStatus)
                OverviewRow("Modalità", uiState.climate.mode)
                OverviewRow("Temperatura Impostata", "${uiState.climate.targetTemp} °C")

                if (uiState.logic.tempoAnticiclo > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⏱️ Isteresi spegnimento: ancora ${uiState.logic.tempoAnticiclo} min",
                        color = Amber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // 4. Ventilazione
        DashboardCard(title = "Ventilazione", accentColor = GreenActive) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewRow("Velocità Attuale", uiState.vmc.fanSpeed.toString())
                OverviewRow("Portata Stimata", "${uiState.logic.portataVmc} m³/h")
            }
        }

        if (uiState.lastUpdateTime.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ultimo aggiornamento: ${uiState.lastUpdateTime}",
                style = MaterialTheme.typography.labelMedium,
                color = GreyText.copy(alpha = 0.6f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun OverviewRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = GreyText, fontSize = 13.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

private fun getForecastLabel(dateString: String): String {
    if (dateString.isEmpty()) return "prossimamente"
    return try {
        val date = LocalDate.parse(dateString)
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        when (date) {
            today -> "oggi"
            tomorrow -> "domani"
            else -> date.format(DateTimeFormatter.ofPattern("dd-MM"))
        }
    } catch (e: Exception) {
        "prossimamente"
    }
}
