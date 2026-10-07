package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus


enum class RequestsTab {
    MINE,
    TO_APPROVE,
    HR_INBOX,
    ALL;

    companion object {
        fun forRole(role: UserRole): List<RequestsTab> = when (role) {
            UserRole.EMPLOYEE -> listOf(MINE, TO_APPROVE)
            UserRole.HUMAN_RESOURCES -> listOf(HR_INBOX, ALL)
        }
    }
}

enum class MyRequestsFilter(val label: String, val status: RequestStatus?) {
    ALL("Todas", null),
    PENDING("Pendientes", RequestStatus.IN_PROGRESS),
    UNDER_REVIEW("En revisión", RequestStatus.UNDER_REVIEW),
    APPROVED("Aprobadas", RequestStatus.APPROVED),
    REJECTED("Rechazadas", RequestStatus.REJECTED)
}

enum class AllRequestsFilter(val label: String, val status: RequestStatus) {
    PENDING("Pendientes", RequestStatus.IN_PROGRESS),
    UNDER_REVIEW("En revisión", RequestStatus.UNDER_REVIEW),
    APPROVED("Aprobadas", RequestStatus.APPROVED),
    REJECTED("Rechazadas", RequestStatus.REJECTED)
}


enum class ApprovalSort(val label: String) {
    OLDEST_FIRST("Más antiguas primero"),
    NEWEST_FIRST("Más recientes primero")
}

data class RejectForm(
    val request: Request,
    val reason: String = "",
    val error: String? = null,
    val isSaving: Boolean = false
)

data class RequestsUiState(
    val role: UserRole = UserRole.EMPLOYEE,
    val selectedTab: RequestsTab = RequestsTab.MINE,

    val isLoadingMine: Boolean = false,
    val myRequests: List<Request> = emptyList(),
    val mineError: String? = null,
    val myFilter: MyRequestsFilter = MyRequestsFilter.ALL,

    val isLoadingPending: Boolean = false,
    val pending: List<Request> = emptyList(),
    val pendingError: String? = null,
    val requestTypes: List<RequestType> = emptyList(),
    val typeFilter: RequestType? = null,
    val sort: ApprovalSort = ApprovalSort.OLDEST_FIRST,
    val processingRequestId: Long? = null,
    val rejectForm: RejectForm? = null,

    val isLoadingAll: Boolean = false,
    val allRequests: List<Request> = emptyList(),
    val allError: String? = null,
    val allFilter: AllRequestsFilter = AllRequestsFilter.PENDING,

    val snackbarMessage: String? = null
) {
    val tabs: List<RequestsTab> get() = RequestsTab.forRole(role)

    val visibleMyRequests: List<Request>
        get() = myFilter.status?.let { status -> myRequests.filter { it.status == status } } ?: myRequests

    val visiblePending: List<Request>
        get() = when (sort) {
            ApprovalSort.OLDEST_FIRST -> pending.sortedBy { it.submittedAt }
            ApprovalSort.NEWEST_FIRST -> pending.sortedByDescending { it.submittedAt }
        }

    val visibleAll: List<Request> get() = allRequests.filter { it.status == allFilter.status }

    fun tabLabel(tab: RequestsTab): String = when (tab) {
        RequestsTab.MINE -> "Mías"
        RequestsTab.TO_APPROVE -> "Por aprobar (${pending.size})"
        RequestsTab.HR_INBOX -> "RR.HH. (${pending.size})"
        RequestsTab.ALL -> "Todas"
    }
}
