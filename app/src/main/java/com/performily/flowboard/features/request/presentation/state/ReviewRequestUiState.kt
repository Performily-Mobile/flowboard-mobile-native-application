package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.entity.VacationAvailability
import com.performily.flowboard.features.request.domain.valueobject.ApproverType


data class ReviewRequestUiState(
    val requestId: Long? = null,
    val viewerId: Long = 0,
    val role: UserRole = UserRole.EMPLOYEE,
    val isLoading: Boolean = false,
    val request: Request? = null,
    val type: RequestType? = null,
    val availability: VacationAvailability? = null,
    val errorMessage: String? = null,
    val isRejectDialogVisible: Boolean = false,
    val rejectReason: String = "",
    val rejectError: String? = null,
    val isReturnSheetVisible: Boolean = false,
    val returnComment: String = "",
    val returnError: String? = null,
    val isProcessing: Boolean = false,
    val snackbarMessage: String? = null,
    val resultMessage: String? = null
) {
    val canResolve: Boolean
        get() {
            val current = request ?: return false
            if (!current.isPending) return false
            return when (current.approverType) {
                ApproverType.HR_STAFF -> role == UserRole.HUMAN_RESOURCES
                ApproverType.DIRECT_MANAGER -> current.approverEmployeeId == viewerId
            }
        }
}
