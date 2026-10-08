package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal

/** MA-75 · Las tres tarjetas del histórico: Máximo, Mínimo y Promedio. */
@Composable
fun StatCards(
    metricType: MetricType,
    maximum: BigDecimal?,
    minimum: BigDecimal?,
    average: BigDecimal?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatCard("Máximo", maximum, metricType, Modifier.weight(1f))
        StatCard("Mínimo", minimum, metricType, Modifier.weight(1f))
        StatCard("Promedio", average, metricType, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(
    label: String,
    value: BigDecimal?,
    metricType: MetricType,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier, contentPadding = 12.dp) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value?.let(WellbeingFormatters::number) ?: "—",
            style = MaterialTheme.typography.titleLarge
        )
        Text(metricType.unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
