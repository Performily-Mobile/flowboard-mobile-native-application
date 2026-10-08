package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.ui.graphics.Color
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.AwaitingContainer
import com.performily.flowboard.core.designsystem.theme.DialogContainer
import com.performily.flowboard.core.designsystem.theme.ErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnErrorContainer
import com.performily.flowboard.core.designsystem.theme.OnSecondaryContainer
import com.performily.flowboard.core.designsystem.theme.Outline
import com.performily.flowboard.core.designsystem.theme.Primary
import com.performily.flowboard.core.designsystem.theme.SecondaryContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerLow
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus

internal object RequestColors {
    val Pending = AwaitingContainer
    val UnderReview = SurfaceContainerLow
    val Approved = ActiveContainer
    val Rejected = Color(0xFFFFCBC5)
    val Cancelled = SurfaceContainerHigh

    val TypeActive = ActiveContainer
    val TypeInactive = SurfaceContainerLow

    val OkBanner = Color(0xFFE8F5E9)
    val OnOkBanner = Color(0xFF2E7D32)

    val ErrorBanner = ErrorContainer
    val OnErrorBanner = OnErrorContainer

    val InfoBanner = SecondaryContainer
    val OnInfoBanner = OnSecondaryContainer

    val CommentCard = SecondaryContainer
    val OnCommentCard = OnSecondaryContainer

    val TimelinePast = Outline
    val TimelineCurrent = Primary

    val Dialog = DialogContainer
    val Sheet = Color(0xFFF1F3FA)

    val AttachmentZone = Color(0xFFECEEF4)

    fun status(status: RequestStatus): Color = when (status) {
        RequestStatus.IN_PROGRESS -> Pending
        RequestStatus.UNDER_REVIEW -> UnderReview
        RequestStatus.APPROVED -> Approved
        RequestStatus.REJECTED -> Rejected
        RequestStatus.CANCELLED -> Cancelled
    }
}
