package com.performily.flowboard.features.benefits.domain.repository

import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.entity.AssignmentBatch
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentTarget
import java.math.BigDecimal
import java.time.LocalDate

interface BenefitAssignmentRepository {
    suspend fun getAssignments(status: AssignmentStatus?): Result<List<BenefitAssignment>>
    suspend fun getEmployeeBenefits(employeeId: Long): Result<EmployeeBenefits>
    suspend fun previewAreaAssignment(benefitTypeId: Long, areaId: Long, startDate: LocalDate, endDate: LocalDate): Result<AreaAssignmentPreview>
    suspend fun assign(
        benefitTypeId: Long,
        target: AssignmentTarget,
        quantity: BigDecimal,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<AssignmentBatch>
    suspend fun registerDelivery(assignmentId: Long, deliveredOn: LocalDate, registeredById: Long, notes: String?): Result<BenefitAssignment>
}
