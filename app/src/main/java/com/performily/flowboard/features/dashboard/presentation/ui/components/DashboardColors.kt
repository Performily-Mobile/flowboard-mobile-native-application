package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.Error
import com.performily.flowboard.core.designsystem.theme.statusColors

/**
 * Color families used to tint the dashboard indicators.
 *
 * The colors come from the theme (Material roles and the status palette), so the icon chips keep
 * their contrast in both the light and the dark theme.
 */
internal enum class DashboardAccent {
    PRIMARY,
    AMBER,
    RED,
    GREEN,
    PURPLE;

    /** Background of the icon chip. */
    val container: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            PRIMARY -> MaterialTheme.colorScheme.primaryContainer
            AMBER -> MaterialTheme.statusColors.warningContainer
            RED -> MaterialTheme.statusColors.dangerContainer
            GREEN -> MaterialTheme.statusColors.successContainer
            PURPLE -> MaterialTheme.colorScheme.tertiaryContainer
        }

    /** Color of the icon drawn on the chip. */
    val content: Color
        @Composable
        @ReadOnlyComposable
        get() = when (this) {
            PRIMARY -> MaterialTheme.colorScheme.onPrimaryContainer
            AMBER -> MaterialTheme.statusColors.onWarningContainer
            RED -> MaterialTheme.statusColors.onDangerContainer
            GREEN -> MaterialTheme.statusColors.onSuccessContainer
            PURPLE -> MaterialTheme.colorScheme.onTertiaryContainer
        }
}

/** Bar colors of the attendance rows, from healthy to worrying. */
internal object DashboardColors {

    /** Bar color of healthy values, such as punctual days. */
    val healthy = Color(0xFF2E7D32)

    /** Bar color of values that deserve attention, such as late days. */
    val warning = Color(0xFFB26A00)

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
