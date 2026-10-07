package com.performily.flowboard.features.payroll.domain.entity

/** Área de la organización, usada solo para filtrar el reporte de pagos. Viene de Workspace por el ACL. */
data class PayrollArea(
    val id: Long,
    val name: String,
    val active: Boolean
)
