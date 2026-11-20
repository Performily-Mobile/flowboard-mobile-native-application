package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.AwaitingContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnErrorContainer
import com.performily.flowboard.core.designsystem.theme.Outline
import com.performily.flowboard.core.designsystem.theme.Primary
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerLow
import com.performily.flowboard.core.designsystem.theme.statusColors
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus

internal object RequestColors {
    val Pending = AwaitingContainer
    val UnderReview = SurfaceContainerLow
    val Approved = ActiveContainer
    val Rejected = Color(0xFFFFCBC5)
    val Cancelled = SurfaceContainerHigh

    val TypeActive = ActiveContainer
    val TypeInactive = SurfaceContainerLow

    val OkBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.statusColors.successContainer
    val OnOkBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.statusColors.onSuccessContainer

    val ErrorBanner = ErrorContainer
    val OnErrorBanner = OnErrorContainer

    val InfoBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
    val OnInfoBanner: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

    val CommentCard: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
    val OnCommentCard: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSecondaryContainer

    val TimelinePast = Outline
    val TimelineCurrent = Primary

    val Dialog: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val Sheet: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLow

    val AttachmentZone: Color
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer

    fun status(status: RequestStatus): Color = when (status) {
        RequestStatus.IN_PROGRESS -> Pending
        RequestStatus.UNDER_REVIEW -> UnderReview
        RequestStatus.APPROVED -> Approved
        RequestStatus.REJECTED -> Rejected
        RequestStatus.CANCELLED -> Cancelled
    }
}
