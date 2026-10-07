package com.performily.flowboard.features.benefits.domain.valueobject

/** Ciclo de vida de una asignación: ASSIGNED pasa a DELIVERED o CANCELLED, que son finales. */
enum class AssignmentStatus(val label: String) {
    ASSIGNED("Asignado"),
    DELIVERED("Entregado"),
    CANCELLED("Anulado")
}
