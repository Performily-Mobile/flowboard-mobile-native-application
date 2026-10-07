package com.performily.flowboard.features.payroll.infrastructure.remote

/** Datos del colaborador que Payroll lee de GET /employees (Workspace). El resto de campos se ignora. */
data class PayrollEmployeeDto(
    val id: Long,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val areaId: Long?
)
