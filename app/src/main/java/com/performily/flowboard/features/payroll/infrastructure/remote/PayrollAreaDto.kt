package com.performily.flowboard.features.payroll.infrastructure.remote

/** Datos del área que Payroll lee de GET /areas (Workspace). */
data class PayrollAreaDto(
    val id: Long,
    val name: String,
    val active: Boolean
)
