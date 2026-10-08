package com.performily.flowboard.features.payroll.infrastructure.repository

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.infrastructure.mapper.PayslipMapper
import com.performily.flowboard.features.payroll.infrastructure.remote.PayslipService
import java.time.LocalDate
import javax.inject.Inject

class PayslipRepositoryImpl @Inject constructor(
    private val service: PayslipService
) : PayslipRepository {

    override suspend fun getPayslipsByPeriod(payrollPeriodId: Long): Result<List<Payslip>> =
        apiCall { service.getPayslipsByPayrollPeriod(payrollPeriodId) }
            .mapCatching { dtos -> dtos.map { PayslipMapper.toDomain(it) } }

    override suspend fun uploadPayslip(
        employeeId: EmployeeId,
        payrollPeriodId: Long,
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): Result<Payslip> {
        val request = PayslipMapper.toUploadRequest(employeeId, payrollPeriodId, file, issueDate, netAmount)
        return apiCall { service.uploadPayslip(request) }
            .mapCatching { PayslipMapper.toDomain(it) }
    }

    override suspend fun replaceFile(
        payslipId: Long,
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): Result<Payslip> {
        val request = PayslipMapper.toReplaceFileRequest(file, issueDate, netAmount)
        return apiCall { service.replacePayslipFile(payslipId, request) }
            .mapCatching { PayslipMapper.toDomain(it) }
    }

    override suspend fun markAsPaid(payslipId: Long, paidOn: LocalDate): Result<Payslip> =
        apiCall { service.markAsPaid(payslipId, PayslipMapper.toMarkAsPaidRequest(paidOn)) }
            .mapCatching { PayslipMapper.toDomain(it) }

    override suspend fun markAsObserved(payslipId: Long, reason: String): Result<Payslip> =
        apiCall { service.markAsObserved(payslipId, PayslipMapper.toMarkAsObservedRequest(reason)) }
            .mapCatching { PayslipMapper.toDomain(it) }
}
