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
import kotlin.math.ceil

private const val WEEK_DAYS = 7

/**
 * MA-75 - Bars with the average of each day.
 *
 * Poor or hazardous days are painted in another color so they stand out. With 7 days or fewer
 * the label is the weekday initial (L M M J V S D); with more days only about seven evenly
 * spaced labels are shown, using the day of the month, so they do not overlap. The bars are
 * scaled between the lowest value (or zero) and the highest value (or zero), so negative
 * averages, such as temperatures below zero, remain visible.
 *
 * @param metricType metric being charted
 * @param dailyAverages one average per day, in ascending date order
 */
@Composable
fun DailyAverageChart(
    metricType: MetricType,
    dailyAverages: List<DailyAverage>,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier) {
        Text(WellbeingFormatters.chartTitle(metricType), style = MaterialTheme.typography.titleSmall)

        val maxValue = dailyAverages.maxOfOrNull { it.average }?.max(BigDecimal.ZERO) ?: BigDecimal.ZERO
        val minValue = dailyAverages.minOfOrNull { it.average }?.min(BigDecimal.ZERO) ?: BigDecimal.ZERO
        val useWeekdays = dailyAverages.size <= WEEK_DAYS
        val labelStep = if (useWeekdays) 1 else ceil(dailyAverages.size / WEEK_DAYS.toDouble()).toInt()

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = WellbeingFormatters.number(maxValue),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Bars(
                values = dailyAverages,
                minValue = minValue,
                maxValue = maxValue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            if (minValue.signum() < 0) {
                Text(
                    text = WellbeingFormatters.number(minValue),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                dailyAverages.forEachIndexed { index, day ->
                    val label = when {
                        index % labelStep != 0 -> ""
                        useWeekdays -> WellbeingFormatters.weekdayInitial(day.date)
                        else -> day.date.dayOfMonth.toString()
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
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

/**
 * Draws the baseline and one bar per day.
 *
 * @param minValue lowest value of the scale, zero or negative
 * @param maxValue highest value of the scale, zero or positive
 */
@Composable
private fun Bars(values: List<DailyAverage>, minValue: BigDecimal, maxValue: BigDecimal, modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawLine(
            color = Divider,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.dp.toPx()
        )
        if (values.isEmpty()) return@Canvas

        val slot = size.width / values.size
        val barWidth = (slot * 0.6f).coerceAtMost(32.dp.toPx())
        val low = minValue.toFloat()
        val span = (maxValue.toFloat() - low).takeIf { it > 0f } ?: 1f

        values.forEachIndexed { index, day ->
            val ratio = ((day.average.toFloat() - low) / span).coerceIn(0f, 1f)
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

/** Colored square with its label, used in the chart legend. */
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
