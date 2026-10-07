package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import javax.inject.Inject

/**
 * Reporte de pagos por período, área y estado (US46).
 * Solo cuentan las boletas publicadas: el estado de pago no se registra en una boleta por publicar.
 * El área de cada colaborador la define Workspace.
 */
class GetPaymentStatusReportUseCase @Inject constructor(
    private val repository: PayslipRepository,
    private val employeeDirectory: PayrollEmployeeDirectory
) {

    suspend operator fun invoke(
        payrollPeriodId: Long,
        areaId: Long?,
        status: PaymentStatus?
    ): Result<List<PayslipEntry>> =
        repository.getPayslipsByPeriod(payrollPeriodId).mapCatching { payslips ->
            val employees = if (areaId != null) {
                // Sin Workspace no se puede filtrar por área: el error se muestra al usuario.
                employeeDirectory.getEmployees().getOrThrow()
            } else {
                employeeDirectory.getEmployees().getOrDefault(emptyList())
            }.associateBy { it.id }

            payslips
                .asSequence()
                .filter { it.isPublished }
                .filter { status == null || it.payment.status == status }
                .map { PayslipEntry(it, employees[it.employeeId]) }
                .filter { areaId == null || it.employee?.areaId == areaId }
                .sortedBy { (it.employee?.sortableName ?: "").lowercase() }
                .toList()
        }
}
