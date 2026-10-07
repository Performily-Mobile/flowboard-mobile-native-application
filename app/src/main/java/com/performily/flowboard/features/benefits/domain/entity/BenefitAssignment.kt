package com.performily.flowboard.features.benefits.domain.entity

import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import java.math.BigDecimal
import java.time.LocalDate

/** Un beneficio asignado a un colaborador (US38), con su entrega cuando ya se hizo (US39). */
data class BenefitAssignment(
    val id: Long,
    val benefitTypeId: Long,
    val benefitTypeName: String,
    val unit: BenefitUnit,
    val employeeId: Long,
    val employeeName: String,
    val sourceAreaId: Long?,
    val quantity: BigDecimal,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val status: AssignmentStatus,
    val delivery: BenefitDelivery?
) {
    /** Solo una asignación pendiente admite registrar su entrega, y una sola vez. */
    val canBeDelivered: Boolean get() = status == AssignmentStatus.ASSIGNED
}

data class BenefitDelivery(
    val deliveredOn: LocalDate,
    val registeredById: Long?,
    val notes: String?
)
