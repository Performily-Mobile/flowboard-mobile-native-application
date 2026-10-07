package com.performily.flowboard.features.benefits.infrastructure.mapper

import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.entity.AssignmentBatch
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.BenefitDelivery
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentTarget
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.infrastructure.remote.AreaAssignmentPreviewDto
import com.performily.flowboard.features.benefits.infrastructure.remote.AssignBenefitRequestDto
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitAssignmentBatchDto
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitAssignmentDto
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitTypeDto
import com.performily.flowboard.features.benefits.infrastructure.remote.CreateBenefitTypeRequestDto
import com.performily.flowboard.features.benefits.infrastructure.remote.EmployeeBenefitsDto
import com.performily.flowboard.features.benefits.infrastructure.remote.RegisterDeliveryRequestDto
import java.math.BigDecimal
import java.time.LocalDate

/** Convierte los DTO de beneficios a entidades del dominio y arma los requests. */
object BenefitsMapper {

    fun toDomain(dto: BenefitTypeDto): BenefitType = BenefitType(
        id = dto.id,
        name = dto.name,
        description = dto.description?.takeIf { it.isNotBlank() },
        unit = BenefitsEnumMapper.unit(dto.unit),
        hasBalance = dto.hasBalance,
        active = dto.active
    )

    fun toCreateRequest(name: String, description: String?, unit: BenefitUnit, hasBalance: Boolean) =
        CreateBenefitTypeRequestDto(name = name, description = description, hasBalance = hasBalance, unit = unit.name)

    fun toDomain(dto: BenefitAssignmentDto): BenefitAssignment = BenefitAssignment(
        id = dto.id,
        benefitTypeId = dto.benefitTypeId,
        benefitTypeName = dto.benefitTypeName.orEmpty(),
        unit = BenefitsEnumMapper.unit(dto.unit),
        employeeId = dto.employeeId,
        employeeName = dto.employeeName?.takeIf { it.isNotBlank() } ?: "Colaborador #${dto.employeeId}",
        sourceAreaId = dto.sourceAreaId,
        quantity = dto.quantity,
        startDate = LocalDate.parse(dto.startDate),
        endDate = LocalDate.parse(dto.endDate),
        status = BenefitsEnumMapper.status(dto.status),
        delivery = dto.delivery?.let { delivery ->
            BenefitDelivery(
                deliveredOn = LocalDate.parse(delivery.deliveredOn),
                registeredById = delivery.registeredById,
                notes = delivery.notes?.takeIf { it.isNotBlank() }
            )
        }
    )

    fun toDomain(dto: EmployeeBenefitsDto): EmployeeBenefits = EmployeeBenefits(
        employeeId = dto.employeeId,
        current = dto.current.orEmpty().map(::toDomain),
        delivered = dto.delivered.orEmpty().map(::toDomain)
    )

    fun toDomain(dto: AreaAssignmentPreviewDto): AreaAssignmentPreview = AreaAssignmentPreview(
        areaId = dto.areaId,
        activeEmployees = dto.activeEmployees,
        alreadyAssigned = dto.alreadyAssigned,
        toAssign = dto.toAssign
    )

    fun toDomain(dto: BenefitAssignmentBatchDto): AssignmentBatch = AssignmentBatch(
        areaId = dto.areaId,
        assignedCount = dto.assignedCount,
        skippedCount = dto.skippedCount,
        assignments = dto.assignments.orEmpty().map(::toDomain)
    )

    fun toAssignRequest(
        benefitTypeId: Long,
        target: AssignmentTarget,
        quantity: BigDecimal,
        startDate: LocalDate,
        endDate: LocalDate
    ) = AssignBenefitRequestDto(
        benefitTypeId = benefitTypeId,
        employeeId = (target as? AssignmentTarget.Employee)?.employeeId,
        areaId = (target as? AssignmentTarget.Area)?.areaId,
        quantity = quantity,
        startDate = startDate.toString(),
        endDate = endDate.toString()
    )

    fun toDeliveryRequest(deliveredOn: LocalDate, registeredById: Long, notes: String?) =
        RegisterDeliveryRequestDto(deliveredOn = deliveredOn.toString(), registeredById = registeredById, notes = notes)

    fun statusParam(status: AssignmentStatus?): String? = status?.name
}
