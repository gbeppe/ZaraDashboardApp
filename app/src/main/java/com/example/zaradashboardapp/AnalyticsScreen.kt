package com.example.zaradashboardapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch

data class ChartSeries(
    val data: List<Float>,
    val color: Color,
    val label: String,
)

@Composable
fun AnalyticsScreen(uiState: SystemState) {
    var selectedChart by remember { mutableStateOf("Batteria") }
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Text(
                text = "Analisi di Sistema",
                color = TealPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf("Batteria", "Temperature", "Humidex", "Bilancio", "Clima", "VMC")
                    items(chips) { chip ->
                        FilterChip(
                            selected = selectedChart == chip,
                            onClick = { selectedChart = chip },
                            label = { Text(chip) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TealPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = TealPrimary,
                                labelColor = GreyText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedChart == chip,
                                borderColor = Color.Transparent,
                                selectedBorderColor = TealPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                when (selectedChart) {
                    "Batteria" -> TrendChartCard(
                        title = "SOC Batteria (%)",
                        dataSeries = listOf(ChartSeries(uiState.batteryHistory, GreenActive, "Batteria"))
                    )
                    "Temperature" -> TrendChartCard(
                        title = "Temperature Ambientali (°C)",
                        dataSeries = listOf(
                            ChartSeries(uiState.tempLivingHistory, TealPrimary, "Living"),
                            ChartSeries(uiState.tempBedroomHistory, Purple80, "Camera"),
                            ChartSeries(uiState.tempOutdoorHistory, OrangeAccent, "Esterno")
                        )
                    )
                    "Humidex" -> TrendChartCard(
                        title = "Indice Humidex",
                        dataSeries = listOf(
                            ChartSeries(uiState.humidexHistory, Amber, "Living"),
                            ChartSeries(uiState.humidexBedroomHistory, Purple80, "Camera")
                        )
                    )
                    "Bilancio" -> TrendChartCard(
                        title = "Bilancio Energetico (W)",
                        dataSeries = listOf(ChartSeries(uiState.surplusHistory, SkyBlue, "Bilancio"))
                    )
                    "Clima" -> TrendChartCard(
                        title = "AC / Heat Pump (°C)",
                        dataSeries = listOf(ChartSeries(uiState.acTargetTempHistory, SkyBlue, "Target Temp"))
                    )
                    "VMC" -> TrendChartCard(
                        title = "Velocità VMC",
                        dataSeries = listOf(ChartSeries(uiState.vmcFanSpeedHistory, TealPrimary, "Velocità"))
                    )
                }
            }
        }

        item {
            Text(
                text = "Registro Eventi",
                color = TealPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(
            items = uiState.recentLogs.reversed(),
            key = { it.id }
        ) { log ->
            val reversedLogs = uiState.recentLogs.reversed()
            val index = reversedLogs.indexOf(log)
            EventTimelineItem(
                log = log,
                isLast = (index == reversedLogs.size - 1)
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TrendChartCard(
    title: String,
    dataSeries: List<ChartSeries>
) {
    val textMeasurer = rememberTextMeasurer()
    val mainColor = dataSeries.firstOrNull()?.color ?: TealPrimary
    
    DashboardCard(title = title, accentColor = mainColor) {
        val allPoints = dataSeries.flatMap { it.data }
        
        if (allPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "In attesa dei dati...",
                    color = GreyText,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            val rawMax = allPoints.maxOrNull() ?: 1f
            val rawMin = allPoints.minOrNull() ?: 0f
            val diff = (rawMax - rawMin).coerceAtLeast(0.5f)
            val margin = diff * 0.15f
            
            val maxVal = rawMax + margin
            val minVal = rawMin - margin
            val range = maxVal - minVal

            Column {
                // Legend
                if (dataSeries.size > 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        dataSeries.forEach { series ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(series.color))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(series.label, color = GreyText, fontSize = 10.sp)
                            }
                        }
                    }
                }

                val scrollState = rememberScrollState(initial = Int.MAX_VALUE)
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .horizontalScroll(scrollState)
                ) {
                    // Calcoliamo la larghezza totale del grafico basandoci su 7 giorni
                    // Se 24h occupano la larghezza dello schermo (es. 400dp), 7 giorni saranno 7 * 400dp
                    // Per semplicità usiamo un moltiplicatore fisso proporzionale al numero di punti
                    // Assumendo 1 punto ogni 15 minuti -> 1440/15 = 96 punti per 24h.
                    // Se abbiamo 2000 punti, il grafico sarà molto lungo.
                    
                    val pointsCount = dataSeries.maxOf { it.data.size }
                    val chartWidth = (pointsCount * 10).dp.coerceAtLeast(400.dp)

                    Row(
                        modifier = Modifier
                            .width(chartWidth)
                            .fillMaxHeight()
                            .padding(top = 24.dp)
                    ) {
                        // Y Axis labels (Fisse a sinistra, ma qui le mettiamo nel flusso dello scroll)
                        // Per farle restare fisse servirebbe un Box esterno, ma complica il disegno.
                        // Le mettiamo all'inizio dello scroll.
                        Column(
                            modifier = Modifier
                                .width(45.dp)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = String.format(Locale.US, "%.1f", maxVal),
                                color = GreyText,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f", minVal),
                                color = GreyText,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }

                        // Chart Canvas
                        Canvas(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            val width = size.width
                            val height = size.height

                            // Draw Zero Line
                            if (minVal < 0f && maxVal > 0f) {
                                val zeroY = height - ((0f - minVal) / range * height)
                                drawLine(
                                    color = GreyText.copy(alpha = 0.3f),
                                    start = Offset(0f, zeroY),
                                    end = Offset(width, zeroY),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                )
                            }

                            dataSeries.forEach { series ->
                                if (series.data.size > 1) {
                                    val spaceX = width / (series.data.size - 1)
                                    val points = series.data.mapIndexed { i, value ->
                                        val x = i * spaceX
                                        val y = height - ((value - minVal) / range * height)
                                        Offset(x, y)
                                    }

                                    val path = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        for (i in 1 until points.size) {
                                            val prev = points[i - 1]
                                            val curr = points[i]
                                            val cp1 = Offset((prev.x + curr.x) / 2, prev.y)
                                            val cp2 = Offset((prev.x + curr.x) / 2, curr.y)
                                            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                                        }
                                    }

                                    // Fill
                                    val fillPath = Path().apply {
                                        addPath(path)
                                        lineTo(points.last().x, height)
                                        lineTo(points.first().x, height)
                                        close()
                                    }
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(series.color.copy(alpha = 0.1f), Color.Transparent),
                                            startY = 0f,
                                            endY = height
                                        )
                                    )

                                    // Line
                                    drawPath(
                                        path = path,
                                        color = series.color,
                                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )

                                    // Last Point
                                    val lastPoint = points.last()
                                    val lastValue = series.data.last()
                                    
                                    drawCircle(color = series.color.copy(alpha = 0.3f), radius = 6.dp.toPx(), center = lastPoint)
                                    drawCircle(color = series.color, radius = 3.dp.toPx(), center = lastPoint)
                                    drawCircle(color = Color.White, radius = 1.5.dp.toPx(), center = lastPoint)

                                    // Value Text
                                    val textLayoutResult = textMeasurer.measure(
                                        text = String.format(Locale.US, "%.1f", lastValue),
                                        style = TextStyle(color = series.color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    )
                                    
                                    val yOffset = if (dataSeries.indexOf(series) % 2 == 0) 12.dp.toPx() else 24.dp.toPx()
                                    
                                    drawText(
                                        textLayoutResult = textLayoutResult,
                                        topLeft = Offset(
                                            x = lastPoint.x - (textLayoutResult.size.width / 2),
                                            y = lastPoint.y - textLayoutResult.size.height - yOffset
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("7 giorni fa", color = GreyText, fontSize = 10.sp)
                    Text("Scorri per navigare lo storico", color = TealPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    
                    val scope = rememberCoroutineScope()
                    Text(
                        text = "Ora",
                        color = TealPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .clickable {
                                scope.launch {
                                    scrollState.animateScrollTo(scrollState.maxValue)
                                }
                            }
                            .background(TealPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EventTimelineItem(log: LogEvent, isLast: Boolean) {
    val dotColor = when (log.eventType) {
        "AI_ENGINE" -> GreenActive
        "ACTION" -> Amber
        "ERROR" -> Crimson
        "MQTT" -> SkyBlue
        "CONTROL" -> OrangeAccent
        else -> GreyText
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(start = 8.dp, bottom = 20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = log.eventType,
                    color = dotColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
                Text(
                    text = log.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                    color = GreyText,
                    fontSize = 11.sp
                )
            }
            
            Text(
                text = log.message,
                color = OffWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Text(
                text = log.details,
                color = GreyText,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
