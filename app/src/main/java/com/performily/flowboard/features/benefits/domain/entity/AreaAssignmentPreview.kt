package com.performily.flowboard.features.benefits.domain.entity

/**
 * Vista previa de asignar a un área (MA-61): cuántos colaboradores activos tiene,
 * cuántos ya tienen el beneficio en el periodo (se omiten) y cuántos lo recibirán.
 */
data class AreaAssignmentPreview(
    val areaId: Long,
    val activeEmployees: Int,
    val alreadyAssigned: Int,
    val toAssign: Int
)
