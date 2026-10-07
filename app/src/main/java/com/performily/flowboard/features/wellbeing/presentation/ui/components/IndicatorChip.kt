package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator

/**
 * Chip del indicador de salud. Sin indicador muestra [emptyLabel]: "Sin dato" cuando
 * la información no está actualizada, "Sin umbral" cuando la métrica no tiene rangos.
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

@Composable
fun WellbeingChip(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface)
    }
}
