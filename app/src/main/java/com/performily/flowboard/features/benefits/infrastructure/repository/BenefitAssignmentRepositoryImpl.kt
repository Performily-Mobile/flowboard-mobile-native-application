package com.performily.flowboard.features.benefits.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.entity.AssignmentBatch
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.repository.BenefitAssignmentRepository
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentTarget
import com.performily.flowboard.features.benefits.infrastructure.mapper.BenefitsMapper
import com.performily.flowboard.features.benefits.infrastructure.remote.BenefitService
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

class BenefitAssignmentRepositoryImpl @Inject constructor(
    private val service: BenefitService
) : BenefitAssignmentRepository {

    override suspend fun getAssignments(status: AssignmentStatus?): Result<List<BenefitAssignment>> =
        apiCall { service.getAssignments(BenefitsMapper.statusParam(status)) }
            .mapCatching { dtos -> dtos.map { BenefitsMapper.toDomain(it) } }

    override suspend fun getEmployeeBenefits(employeeId: Long): Result<EmployeeBenefits> =
        apiCall { service.getMyBenefits(employeeId) }.mapCatching { BenefitsMapper.toDomain(it) }

    override suspend fun previewAreaAssignment(
        benefitTypeId: Long,
        areaId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<AreaAssignmentPreview> =
        apiCall { service.previewAreaAssignment(benefitTypeId, areaId, startDate.toString(), endDate.toString()) }
            .mapCatching { BenefitsMapper.toDomain(it) }

    override suspend fun assign(
        benefitTypeId: Long,
        target: AssignmentTarget,
        quantity: BigDecimal,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<AssignmentBatch> {
        val request = BenefitsMapper.toAssignRequest(benefitTypeId, target, quantity, startDate, endDate)
        return apiCall { service.assign(request) }.mapCatching { BenefitsMapper.toDomain(it) }
    }

    override suspend fun registerDelivery(
        assignmentId: Long,
        deliveredOn: LocalDate,
        registeredById: Long,
        notes: String?
    ): Result<BenefitAssignment> {
        val request = BenefitsMapper.toDeliveryRequest(deliveredOn, registeredById, notes)
        return apiCall { service.registerDelivery(assignmentId, request) }.mapCatching { BenefitsMapper.toDomain(it) }
    }
}
