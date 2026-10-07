package com.performily.flowboard.features.benefits.infrastructure.remote

import java.math.BigDecimal

data class BenefitAssignmentDto(
    val id: Long,
    val benefitTypeId: Long,
    val benefitTypeName: String?,
    val unit: String,
    val employeeId: Long,
    val employeeName: String?,
    val sourceAreaId: Long?,
    val quantity: BigDecimal,
    val displayQuantity: String?,
    val startDate: String,
    val endDate: String,
    val status: String,
    val delivery: BenefitDeliveryDto?
)

data class BenefitDeliveryDto(
    val deliveredOn: String,
    val registeredById: Long?,
    val notes: String?
)

data class BenefitAssignmentBatchDto(
    val areaId: Long?,
    val assignedCount: Int,
    val skippedCount: Int,
    val skippedEmployeeIds: List<Long>?,
    val assignments: List<BenefitAssignmentDto>?
)

data class EmployeeBenefitsDto(
    val employeeId: Long,
    val currentCount: Int,
    val deliveredCount: Int,
    val current: List<BenefitAssignmentDto>?,
    val delivered: List<BenefitAssignmentDto>?
)

data class AreaAssignmentPreviewDto(
    val areaId: Long,
    val activeEmployees: Int,
    val alreadyAssigned: Int,
    val toAssign: Int
)

/** Se envía employeeId o areaId, no ambos. Gson omite el que va en null. */
data class AssignBenefitRequestDto(
    val benefitTypeId: Long,
    val employeeId: Long?,
    val areaId: Long?,
    val quantity: BigDecimal,
    val startDate: String,
    val endDate: String
)

data class RegisterDeliveryRequestDto(
    val deliveredOn: String,
    val registeredById: Long?,
    val notes: String?
)
