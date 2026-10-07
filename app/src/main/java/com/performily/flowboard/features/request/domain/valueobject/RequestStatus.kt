package com.performily.flowboard.features.request.domain.valueobject

/**
 * Estado de una solicitud, igual que en el backend.
 * IN_PROGRESS: esperando la decisión del aprobador ("Pendiente" en el prototipo).
 * UNDER_REVIEW: el aprobador la devolvió para que el colaborador la corrija.
 * APPROVED, REJECTED y CANCELLED son finales.
 *
 * @property label texto del chip de estado (MA-37, MA-42, MA-53)
 */
enum class RequestStatus(val label: String) {
    IN_PROGRESS("Pendiente"),
    UNDER_REVIEW("En revisión"),
    APPROVED("Aprobada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada");

    val isFinal: Boolean get() = this == APPROVED || this == REJECTED || this == CANCELLED
}
