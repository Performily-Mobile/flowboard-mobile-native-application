package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.wellbeing.domain.entity.MetricStatus
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus

/**
 * MA-70 · Tarjeta de un espacio con su indicador general y las tres métricas.
 * Si no hay lecturas recientes muestra "Sin dato" y cuándo fue la última lectura.
 */
@Composable
fun OfficeCard(
    status: OfficeStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = status.office.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = status.office.location.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IndicatorChip(
                indicator = if (status.upToDate) status.overallIndicator else null,
                emptyLabel = if (status.upToDate) "Sin umbral" else "Sin dato"
            )
        }

        if (status.upToDate) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                status.metrics.forEach { metric ->
                    MetricTile(metric = metric, modifier = Modifier.weight(1f))
                }
            }
        } else {
            Text(
                text = staleText(status),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MetricTile(metric: MetricStatus, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = metric.metricType.shortLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = if (metric.upToDate && metric.lastValue != null) {
                WellbeingFormatters.value(metric.lastValue, metric.metricType)
            } else {
                "—"
            },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IndicatorChip(
            indicator = if (metric.upToDate) metric.indicator else null,
            emptyLabel = if (metric.upToDate) "Sin umbral" else "Sin dato"
        )
    }
}

private fun staleText(status: OfficeStatus): String {
    val lastReading = status.lastReadingAt ?: return "Todavía no hay lecturas registradas en este espacio."
    return "La última lectura fue ${WellbeingFormatters.dayAndTime(lastReading)}. La información no está actualizada."
}
