package com.performily.flowboard.features.request.domain.valueobject


enum class RequestStatus(val label: String) {
    IN_PROGRESS("Pendiente"),
    UNDER_REVIEW("En revisión"),
    APPROVED("Aprobada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada");

    val isFinal: Boolean get() = this == APPROVED || this == REJECTED || this == CANCELLED
}
