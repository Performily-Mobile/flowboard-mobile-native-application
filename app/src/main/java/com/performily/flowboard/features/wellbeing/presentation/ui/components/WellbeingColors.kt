package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.PendingContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator

/** Colores de los indicadores de salud (MA-70 a MA-75). Reusa los del tema cuando existen. */
internal object WellbeingColors {
    val Optimal = ActiveContainer
    val Acceptable = Color(0xFFF6D186)
    val Poor = Color(0xFFF3B867)
    val Hazardous = ErrorContainer
    val NoData = SurfaceContainerHigh

    val StaleBanner = PendingContainer
    val OnStaleBanner = Color(0xFF5C3B00)
    val DangerBanner = ErrorContainer
    val OnDangerBanner = Color(0xFF8C1D18)

    val ChartNormal = Color(0xFF39608F)
    val ChartPoor = Color(0xFFC04A00)
    val ChartHazardous = Color(0xFFBA1A1A)

    fun container(indicator: HealthIndicator?): Color = when (indicator) {
        HealthIndicator.OPTIMAL -> Optimal
        HealthIndicator.ACCEPTABLE -> Acceptable
        HealthIndicator.POOR -> Poor
        HealthIndicator.HAZARDOUS -> Hazardous
        null -> NoData
    }

    fun chartBar(indicator: HealthIndicator?): Color = when (indicator) {
        HealthIndicator.POOR -> ChartPoor
        HealthIndicator.HAZARDOUS -> ChartHazardous
        else -> ChartNormal
    }
}
