package com.performily.flowboard.features.request.domain.entity

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.valueobject.ApproverType
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import java.time.LocalDateTime

/**
 * Solicitud de un colaborador (vacaciones, descanso médico, permiso por horas...).
 */
data class Request(
    val id: Long,
    val requesterId: Long,
    val requestTypeId: Long,
    val requestTypeName: String,
    val status: RequestStatus,
    val period: RequestPeriod?,
    val requestedDays: Int,
    val approverType: ApproverType,
    val approverEmployeeId: Long?,
    val submittedAt: LocalDateTime,
    val fieldValues: List<RequestFieldValue>,
    val attachments: List<FileReference>,
    val history: List<RequestHistoryEntry>,
    val requester: RequestPerson? = null,
    val approver: RequestPerson? = null
) {
    val isPending: Boolean get() = status == RequestStatus.IN_PROGRESS
    val isUnderReview: Boolean get() = status == RequestStatus.UNDER_REVIEW
    val isResolved: Boolean get() = status.isFinal


    val approverName: String
        get() = when (approverType) {
            ApproverType.HR_STAFF -> "Recursos Humanos"
            ApproverType.DIRECT_MANAGER -> approver?.name ?: "Jefe directo"
        }

    val requesterName: String get() = requester?.name ?: "Colaborador #$requesterId"


    val lastChange: RequestHistoryEntry? get() = history.lastOrNull { it.newStatus == status }

  
    val reviewComment: RequestHistoryEntry?
        get() = history.lastOrNull { it.newStatus == RequestStatus.UNDER_REVIEW }

    fun value(key: String): String? = fieldValues.firstOrNull { it.key == key }?.value
}

data class RequestFieldValue(
    val key: String,
    val value: String
)


data class RequestHistoryEntry(
    val id: Long,
    val previousStatus: RequestStatus?,
    val newStatus: RequestStatus,
    val actorId: Long?,
    val actorName: String?,
    val comment: String?,
    val occurredAt: LocalDateTime
)
