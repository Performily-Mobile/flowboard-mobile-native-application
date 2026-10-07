package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.wellbeing.domain.entity.DailyAverage
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal

private const val WEEK_DAYS = 7

/**
 * MA-75 · Barras con el promedio de cada día. Los días deficientes o peligrosos
 * se pintan de otro color para que se vean a simple vista.
 * Con 7 días o menos la etiqueta es la inicial del día (L M M J V S D);
 * con más días se usa el número del día del mes.
 */
@Composable
fun DailyAverageChart(
    metricType: MetricType,
    dailyAverages: List<DailyAverage>,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier) {
        Text(WellbeingFormatters.chartTitle(metricType), style = MaterialTheme.typography.titleSmall)

        val maxValue = dailyAverages.maxOfOrNull { it.average }?.takeIf { it > BigDecimal.ZERO } ?: BigDecimal.ONE
        val useWeekdays = dailyAverages.size <= WEEK_DAYS

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = WellbeingFormatters.number(maxValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Bars(
                values = dailyAverages,
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                dailyAverages.forEach { day ->
                    Text(
                        text = if (useWeekdays) WellbeingFormatters.weekdayInitial(day.date) else day.date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = WellbeingColors.ChartNormal, label = "Normal")
            LegendItem(color = WellbeingColors.ChartPoor, label = "Deficiente")
            LegendItem(color = WellbeingColors.ChartHazardous, label = "Peligro")
        }
    }
}

@Composable
private fun Bars(values: List<DailyAverage>, maxValue: BigDecimal, modifier: Modifier) {
    Canvas(modifier = modifier) {
        // Línea base del gráfico.
        drawLine(
            color = Divider,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
        if (values.isEmpty()) return@Canvas

        val slot = size.width / values.size
        val barWidth = (slot * 0.6f).coerceAtMost(32.dp.toPx())
        val max = maxValue.toFloat()

        values.forEachIndexed { index, day ->
            val ratio = (day.average.toFloat() / max).coerceIn(0f, 1f)
            val barHeight = (size.height * ratio).coerceAtLeast(2.dp.toPx())
            drawRoundRect(
                color = WellbeingColors.chartBar(day.indicator),
                topLeft = Offset(slot * index + (slot - barWidth) / 2, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx())
            )
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(2.dp))
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
