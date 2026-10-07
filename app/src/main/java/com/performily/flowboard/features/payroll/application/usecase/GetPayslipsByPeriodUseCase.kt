package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import javax.inject.Inject

/**
 * Boletas cargadas en un período (vista de RR.HH.), con el nombre de su colaborador,
 * en orden alfabético por apellido.
 */
class GetPayslipsByPeriodUseCase @Inject constructor(
    private val repository: PayslipRepository,
    private val employeeDirectory: PayrollEmployeeDirectory
) {

    suspend operator fun invoke(payrollPeriodId: Long): Result<List<PayslipEntry>> =
        repository.getPayslipsByPeriod(payrollPeriodId).map { payslips ->
            // Si Workspace no responde, la lista se muestra igual sin los nombres.
            val employees = employeeDirectory.getEmployees().getOrDefault(emptyList()).associateBy { it.id }
            payslips
                .map { PayslipEntry(it, employees[it.employeeId]) }
                .sortedBy { (it.employee?.sortableName ?: "").lowercase() }
        }
}
