package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.repository.PayrollEmployeeDirectory
import javax.inject.Inject

/** Áreas activas para el filtro del reporte de pagos, en orden alfabético. */
class GetPayrollAreasUseCase @Inject constructor(
    private val employeeDirectory: PayrollEmployeeDirectory
) {

    suspend operator fun invoke(): Result<List<PayrollArea>> =
        employeeDirectory.getAreas().map { areas -> areas.filter { it.active }.sortedBy { it.name.lowercase() } }
}
