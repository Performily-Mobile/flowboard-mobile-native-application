package com.performily.flowboard.features.benefits.presentation.ui.components

import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.OnSurfaceVariant
import com.performily.flowboard.core.designsystem.theme.SecondaryContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus

/** Colores de los chips y avisos de Benefits. Reusa los del tema cuando existen. */
internal object BenefitsColors {
    val Assigned = SecondaryContainer
    val Delivered = ActiveContainer
    val Cancelled = SurfaceContainerHigh
    val Active = ActiveContainer
    val Inactive = SurfaceContainerHigh

    val OfflineBanner = SurfaceContainer
    val OnOfflineBanner = OnSurfaceVariant
    val WarningBanner = Color(0xFFFFF3E0)
    val OnWarningBanner = Color(0xFF5C2400)

    fun status(status: AssignmentStatus): Color = when (status) {
        AssignmentStatus.ASSIGNED -> Assigned
        AssignmentStatus.DELIVERED -> Delivered
        AssignmentStatus.CANCELLED -> Cancelled
    }
}
