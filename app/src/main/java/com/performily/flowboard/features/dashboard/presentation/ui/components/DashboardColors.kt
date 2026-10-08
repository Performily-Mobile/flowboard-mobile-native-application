package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.Error
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnPrimaryContainer
import com.performily.flowboard.core.designsystem.theme.PendingContainer
import com.performily.flowboard.core.designsystem.theme.PrimaryContainer

/**
 * Semantic color pairs used to tint the dashboard indicators.
 *
 * @property container background of the icon chip.
 * @property content color of the icon drawn on the chip.
 */
internal enum class DashboardAccent(val container: Color, val content: Color) {
    PRIMARY(PrimaryContainer, OnPrimaryContainer),
    AMBER(PendingContainer, Color(0xFF7A4B00)),
    RED(ErrorContainer, OnErrorContainer),
    GREEN(ActiveContainer, Color(0xFF1B4D1B))
}

/** Bar colors of the attendance rows, from healthy to worrying. */
internal object DashboardColors {

    private val healthy = Color(0xFF2E7D32)
    private val warning = Color(0xFFB26A00)

    /**
     * Returns the bar color for an attendance percentage.
     *
     * @param percentage value from 0 to 100.
     * @return green from 85, amber from 60 and red below that.
     */
    fun attendanceBar(percentage: Int): Color = when {
        percentage >= 85 -> healthy
        percentage >= 60 -> warning
        else -> Error
    }
}
