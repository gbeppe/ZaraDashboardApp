package com.example.zaradashboardapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
    LazyColumn(
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
            TrendChartCard()
        }

        item {
            Text(
                text = "Registro Eventi",
                color = TealPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(uiState.recentLogs) { index, log ->
            EventTimelineItem(
                log = log,
                isLast = index == uiState.recentLogs.size - 1
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TrendChartCard() {
    DashboardCard(title = "Andamento Energetico (Ultimi 7gg)", accentColor = SkyBlue) {
        val dataPoints = listOf(30f, 45f, 40f, 60f, 55f, 80f, 90f, 85f, 100f)
        
        Column {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(top = 16.dp)
            ) {
                val width = size.width
                val height = size.height
                val maxVal = dataPoints.maxOrNull() ?: 100f
                val minVal = 0f
                
                val spaceX = width / (dataPoints.size - 1)
                
                val points = dataPoints.mapIndexed { i, value ->
                    val x = i * spaceX
                    val y = height - ((value - minVal) / (maxVal - minVal) * height)
                    Offset(x, y)
                }

                // Path for the curve
                val path = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        // Using quadratic bezier for smooth curves
                        val prev = points[i - 1]
                        val curr = points[i]
                        val cp1 = Offset((prev.x + curr.x) / 2, prev.y)
                        val cp2 = Offset((prev.x + curr.x) / 2, curr.y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, curr.x, curr.y)
                    }
                }

                // Fill under the line
                val fillPath = Path().apply {
                    addPath(path)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }

                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(SkyBlue.copy(alpha = 0.3f), Color.Transparent),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw the line
                drawPath(
                    path = path,
                    color = SkyBlue,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                
                // Draw dots at points
                points.forEach { point ->
                    drawCircle(
                        color = DarkBackground,
                        radius = 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = SkyBlue,
                        radius = 3.dp.toPx(),
                        center = point,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("LUN", color = GreyText, fontSize = 10.sp)
                Text("MER", color = GreyText, fontSize = 10.sp)
                Text("DOM", color = GreyText, fontSize = 10.sp)
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
