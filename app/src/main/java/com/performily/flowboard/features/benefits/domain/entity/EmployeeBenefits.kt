package com.performily.flowboard.features.benefits.domain.entity

/**
 * Beneficios de un colaborador (US40).
 * @property current asignados, sin entregar y aún vigentes
 * @property delivered ya entregados, del más reciente al más antiguo
 */
data class EmployeeBenefits(
    val employeeId: Long,
    val current: List<BenefitAssignment>,
    val delivered: List<BenefitAssignment>
)
