package com.performily.flowboard.features.request.domain.valueobject

/**
 * Quién resuelve la solicitud.
 * DIRECT_MANAGER: el jefe directo del colaborador.
 * HR_STAFF: Recursos Humanos, cuando el colaborador no tiene jefe directo (MA-52).
 */
enum class ApproverType {
    DIRECT_MANAGER,
    HR_STAFF
}
