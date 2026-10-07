package com.performily.flowboard.features.request.domain.entity

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.valueobject.ApproverType
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import java.time.LocalDateTime

/**
 * Solicitud de un colaborador (vacaciones, descanso médico, permiso por horas...).
 * Los nombres de las personas no vienen del backend de Request: se completan con
 * Workspace a través de [RequestPerson] (capa anticorrupción).
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

    /** "Henry Cabrera", o "Recursos Humanos" si la atiende RR.HH. */
    val approverName: String
        get() = when (approverType) {
            ApproverType.HR_STAFF -> "Recursos Humanos"
            ApproverType.DIRECT_MANAGER -> approver?.name ?: "Jefe directo"
        }

    val requesterName: String get() = requester?.name ?: "Colaborador #$requesterId"

    /** Último cambio que llevó la solicitud a su estado actual (aprobación, rechazo, devolución...). */
    val lastChange: RequestHistoryEntry? get() = history.lastOrNull { it.newStatus == status }

    /** Comentario con el que se devolvió a revisión (MA-44). */
    val reviewComment: RequestHistoryEntry?
        get() = history.lastOrNull { it.newStatus == RequestStatus.UNDER_REVIEW }

    fun value(key: String): String? = fieldValues.firstOrNull { it.key == key }?.value
}

data class RequestFieldValue(
    val key: String,
    val value: String
)

/** Un cambio de estado del historial (MA-42, MA-44). previousStatus es null en el envío. */
data class RequestHistoryEntry(
    val id: Long,
    val previousStatus: RequestStatus?,
    val newStatus: RequestStatus,
    val actorId: Long?,
    val actorName: String?,
    val comment: String?,
    val occurredAt: LocalDateTime
)
