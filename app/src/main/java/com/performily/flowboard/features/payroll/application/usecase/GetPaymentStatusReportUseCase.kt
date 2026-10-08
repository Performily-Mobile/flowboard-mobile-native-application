package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import javax.inject.Inject

/**
 * Builds the payment report by period, area and status (US46).
 *
 * Only published payslips count, because the payment status is not registered on an unpublished
 * payslip. The area of each employee is defined by Workspace.
 */
class GetPaymentStatusReportUseCase @Inject constructor(
    private val repository: PayslipRepository,
    private val employeeDirectory: PayrollEmployeeDirectory
) {

    /**
     * Returns the published payslips of a period that match the filters.
     *
     * Filtering by area requires Workspace, so its failure is returned to the user in that case.
     * Suspending calls are made outside `mapCatching` so a cancellation is never swallowed.
     *
     * @param payrollPeriodId id of the payroll period
     * @param areaId area to filter by, or null for all areas
     * @param status payment status to filter by, or null for all statuses
     * @return the entries sorted by employee name
     */
    suspend operator fun invoke(
        payrollPeriodId: Long,
        areaId: Long?,
        status: PaymentStatus?
    ): Result<List<PayslipEntry>> {
        val payslips = repository.getPayslipsByPeriod(payrollPeriodId)
            .getOrElse { return Result.failure(it) }
        val employeesResult = employeeDirectory.getEmployees()
        val employees = if (areaId != null) {
            employeesResult.getOrElse { return Result.failure(it) }
        } else {
            employeesResult.getOrDefault(emptyList())
        }.associateBy { it.id }

        return Result.success(
            payslips
                .asSequence()
                .filter { it.isPublished }
                .filter { status == null || it.payment.status == status }
                .map { PayslipEntry(it, employees[it.employeeId]) }
                .filter { areaId == null || it.employee?.areaId == areaId }
                .sortedBy { (it.employee?.sortableName ?: it.payslip.employeeName.orEmpty()).lowercase() }
                .toList()
        )
    }
}
