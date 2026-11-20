package com.performily.flowboard.features.benefits.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.SecondaryContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.core.designsystem.theme.statusColors
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus

/** Colores de los chips y avisos de Benefits. Reusa los del tema cuando existen. */
internal object BenefitsColors {
    val Assigned = SecondaryContainer
    val Delivered = ActiveContainer
    val Cancelled = SurfaceContainerHigh
    val Active = ActiveContainer
    val Inactive = SurfaceContainerHigh

    val OfflineBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
    val OnOfflineBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val WarningBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.statusColors.warningContainer
    val OnWarningBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.statusColors.onWarningContainer

    fun status(status: AssignmentStatus): Color = when (status) {
        AssignmentStatus.ASSIGNED -> Assigned
        AssignmentStatus.DELIVERED -> Delivered
        AssignmentStatus.CANCELLED -> Cancelled
    }
}
