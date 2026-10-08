package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.repository.PayrollPeriodRepository
import javax.inject.Inject

/** Publica todas las boletas del período que siguen por publicar. Devuelve cuántas se publicaron. */
class PublishPeriodPayslipsUseCase @Inject constructor(
    private val repository: PayrollPeriodRepository
) {

    suspend operator fun invoke(payrollPeriodId: Long): Result<Int> =
        repository.publishPayslips(payrollPeriodId)
}
