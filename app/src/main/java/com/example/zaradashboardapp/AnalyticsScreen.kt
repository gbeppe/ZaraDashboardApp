package com.example.zaradashboardapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zaradashboardapp.ui.theme.*
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(uiState: SystemState) {
    var selectedChart by remember { mutableStateOf("Batteria") }
    // Usiamo LazyListState per avere controllo granulare sullo scroll
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val chips = listOf("Batteria", "Humidex", "Bilancio")
                    chips.forEach { chip ->
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

                val (dataPoints, chartColor, chartTitle) = when (selectedChart) {
                    "Batteria" -> Triple(uiState.batteryHistory, GreenActive, "SOC Batteria (%)")
                    "Humidex" -> Triple(uiState.humidexHistory, Amber, "Humidex Soggiorno")
                    else -> Triple(uiState.surplusHistory, SkyBlue, "Bilancio Energetico (W)")
                }

                TrendChartCard(
                    title = chartTitle,
                    dataPoints = dataPoints,
                    lineColor = chartColor
                )
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
                isLast = index == reversedLogs.size - 1
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
    dataPoints: List<Float>,
    lineColor: Color
) {
    DashboardCard(title = title, accentColor = lineColor) {
        if (dataPoints.isEmpty()) {
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
            val maxVal = (dataPoints.maxOrNull() ?: 1f).coerceAtLeast(1f)
            val minVal = dataPoints.minOrNull() ?: 0f
            val range = (maxVal - minVal).coerceAtLeast(1f)

            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(top = 16.dp)
                ) {
                    // Y Axis labels
                    Column(
                        modifier = Modifier
                            .width(45.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = maxVal.toInt().toString(),
                            color = GreyText,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = minVal.toInt().toString(),
                            color = GreyText,
                            fontSize = 10.sp,
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

                        // Draw Zero Line if applicable
                        if (minVal < 0f && maxVal > 0f) {
                            val zeroY = height - ((0f - minVal) / range * height)
                            drawLine(
                                color = GreyText.copy(alpha = 0.5f),
                                start = Offset(0f, zeroY),
                                end = Offset(width, zeroY),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }

                        val spaceX = if (dataPoints.size > 1) width / (dataPoints.size - 1) else width

                        val points = dataPoints.mapIndexed { i, value ->
                            val x = i * spaceX
                            val y = height - ((value - minVal) / range * height)
                            Offset(x, y)
                        }

                        // Path for the curve
                        val path = Path().apply {
                            if (points.isNotEmpty()) {
                                moveTo(points.first().x, points.first().y)
                                for (i in 1 until points.size) {
                                    val prev = points[i - 1]
                                    val curr = points[i]
                                    val cp1 = Offset((prev.x + curr.x) / 2, prev.y)
                                    val cp2 = Offset((prev.x + curr.x) / 2, curr.y)
                                    cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                                }
                            }
                        }

                        if (points.size > 1) {
                            // Fill under the line
                            val fillPath = Path().apply {
                                addPath(path)
                                lineTo(points.last().x, height)
                                lineTo(points.first().x, height)
                                close()
                            }

                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(lineColor.copy(alpha = 0.3f), Color.Transparent),
                                    startY = 0f,
                                    endY = height
                                )
                            )

                            // Draw the line
                            drawPath(
                                path = path,
                                color = lineColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        } else if (points.size == 1) {
                            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = points.first())
                        }

                        // Optional: Draw dots if few points
                        if (dataPoints.size < 20) {
                            points.forEach { point ->
                                drawCircle(
                                    color = DarkBackground,
                                    radius = 3.dp.toPx(),
                                    center = point
                                )
                                drawCircle(
                                    color = lineColor,
                                    radius = 2.dp.toPx(),
                                    center = point,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 45.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Inizio", color = GreyText, fontSize = 10.sp)
                    Text("Tempo reale (sliding)", color = GreyText, fontSize = 10.sp)
                    Text("Ora", color = GreyText, fontSize = 10.sp)
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
        // Timeline Column (Left)
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

        // Content Column (Right)
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
