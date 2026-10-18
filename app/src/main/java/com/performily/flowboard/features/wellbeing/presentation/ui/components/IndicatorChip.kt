package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator

/**
 * Health indicator chip.
 *
 * Without an indicator it shows [emptyLabel]: "Sin dato" when the information is outdated,
 * "Sin umbral" when the metric has no ranges.
 *
 * @param indicator indicator to show, if any.
 * @param modifier modifier applied to the chip.
 * @param emptyLabel text shown when there is no indicator.
 */
@Composable
fun IndicatorChip(
    indicator: HealthIndicator?,
    modifier: Modifier = Modifier,
    emptyLabel: String = "Sin dato"
) {
    WellbeingChip(
        text = indicator?.let(WellbeingFormatters::indicatorLabel) ?: emptyLabel,
        containerColor = WellbeingColors.container(indicator),
        modifier = modifier
    )
}

/**
 * Pill-shaped chip with a colored background.
 *
 * @param text text of the chip.
 * @param containerColor background color.
 * @param modifier modifier applied to the chip.
 */
@Composable
fun WellbeingChip(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(containerColor, CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface)
    }
}
