package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import javax.inject.Inject

/** Registra una incidencia en el depósito de una boleta publicada, con su motivo (US45). */
class MarkPayslipAsObservedUseCase @Inject constructor(
    private val repository: PayslipRepository
) {

    suspend operator fun invoke(payslip: Payslip, reason: String): Result<Payslip> {
        if (!payslip.canUpdatePayment) {
            return Result.failure(IllegalStateException("La boleta debe estar publicada para registrar su pago."))
        }
        return runCatching { PaymentDetails.observed(reason) }
            .fold(
                onSuccess = { details -> repository.markAsObserved(payslip.id, details.observationReason.orEmpty()) },
                onFailure = { Result.failure(it) }
            )
    }
}
