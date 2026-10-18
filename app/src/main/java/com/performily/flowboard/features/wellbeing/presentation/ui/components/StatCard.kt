package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal

/**
 * The three cards of the reading history (MA-75): Máximo, Mínimo and Promedio.
 *
 * @param metricType metric whose unit is shown.
 * @param maximum highest value of the period.
 * @param minimum lowest value of the period.
 * @param average average value of the period.
 * @param modifier modifier applied to the row.
 */
@Composable
fun StatCards(
    metricType: MetricType,
    maximum: BigDecimal?,
    minimum: BigDecimal?,
    average: BigDecimal?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
        StatCard("Máximo", maximum, metricType, Modifier.weight(1f))
        StatCard("Mínimo", minimum, metricType, Modifier.weight(1f))
        StatCard("Promedio", average, metricType, Modifier.weight(1f))
    }
}

/**
 * Small tonal card with a label, a value (or a dash when missing) and the unit.
 *
 * @param label title of the card.
 * @param value value to show.
 * @param metricType metric whose unit is shown.
 * @param modifier modifier applied to the card.
 */
@Composable
fun StatCard(
    label: String,
    value: BigDecimal?,
    metricType: MetricType,
    modifier: Modifier = Modifier
) {
    OutlinedPanel(modifier = modifier, contentPadding = 16.dp) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value?.let(WellbeingFormatters::number) ?: "—",
            style = MaterialTheme.typography.titleLarge
        )
        Text(metricType.unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
