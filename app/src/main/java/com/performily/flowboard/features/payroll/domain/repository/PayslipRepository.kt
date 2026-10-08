package com.performily.flowboard.features.payroll.domain.repository

import com.performily.flowboard.core.domain.EmployeeId
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.payroll.domain.entity.Payslip
import java.time.LocalDate

interface PayslipRepository {

    suspend fun getPayslipsByPeriod(payrollPeriodId: Long): Result<List<Payslip>>

    suspend fun uploadPayslip(
        employeeId: EmployeeId,
        payrollPeriodId: Long,
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): Result<Payslip>

    suspend fun replaceFile(
        payslipId: Long,
        file: FileReference,
        issueDate: LocalDate,
        netAmount: Money
    ): Result<Payslip>

    suspend fun markAsPaid(payslipId: Long, paidOn: LocalDate): Result<Payslip>

    suspend fun markAsObserved(payslipId: Long, reason: String): Result<Payslip>
}
