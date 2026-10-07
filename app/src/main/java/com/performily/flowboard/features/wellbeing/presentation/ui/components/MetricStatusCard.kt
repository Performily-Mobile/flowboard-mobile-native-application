package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performily.flowboard.features.wellbeing.domain.entity.MetricStatus

/**
 * MA-72 / MA-81 · Una métrica de un espacio: valor actual, indicador y rango óptimo.
 * Sin lecturas recientes muestra "—" y "Sin dato", y cuándo fue la última lectura.
 */
@Composable
fun MetricStatusCard(
    metric: MetricStatus,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = metric.metricType.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (metric.upToDate && metric.lastValue != null) {
                        WellbeingFormatters.value(metric.lastValue, metric.metricType)
                    } else {
                        "—"
                    },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IndicatorChip(
                indicator = if (metric.upToDate) metric.indicator else null,
                emptyLabel = if (metric.upToDate) "Sin umbral" else "Sin dato"
            )
        }
        Text(
            text = subtitle(metric),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun subtitle(metric: MetricStatus): String {
    val range = if (metric.optimalMin != null && metric.optimalMax != null) {
        "Rango óptimo: ${WellbeingFormatters.range(metric.optimalMin, metric.optimalMax, metric.metricType)}"
    } else {
        "Sin umbrales configurados"
    }
    val lastReading = metric.lastRecordedAt
    return when {
        metric.upToDate -> range
        lastReading != null -> "$range · última lectura ${WellbeingFormatters.ago(lastReading)}"
        else -> "$range · sin lecturas"
    }
}
