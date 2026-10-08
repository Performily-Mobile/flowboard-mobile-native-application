package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType


data class RequestDetailUiState(
    val requestId: Long? = null,
    val viewerId: Long = 0,
    val isLoading: Boolean = false,
    val request: Request? = null,
    val type: RequestType? = null,
    val errorMessage: String? = null,
    val isCancelDialogVisible: Boolean = false,
    val isCancelling: Boolean = false,
    val attachments: List<FileReference> = emptyList(),
    val isUploading: Boolean = false,
    val attachmentError: String? = null,
    val isResubmitting: Boolean = false,
    val snackbarMessage: String? = null,
    /** Cuando tiene valor, la acción terminó y la pantalla vuelve con este mensaje. */
    val resultMessage: String? = null
) {
    val canCancel: Boolean get() = request != null && !request.isResolved && request.requesterId == viewerId
    val canResubmit: Boolean get() = request != null && request.isUnderReview && request.requesterId == viewerId
}
