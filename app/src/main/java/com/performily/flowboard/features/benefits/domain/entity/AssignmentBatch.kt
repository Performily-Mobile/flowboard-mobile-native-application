package com.performily.flowboard.features.benefits.domain.entity

/** Resultado de una asignación: a cuántos se asignó y a cuántos se omitió por ya tenerlo. */
data class AssignmentBatch(
    val areaId: Long?,
    val assignedCount: Int,
    val skippedCount: Int,
    val assignments: List<BenefitAssignment>
)
