package com.performily.flowboard.features.payroll.application.usecase

import com.performily.flowboard.features.payroll.domain.entity.Payslip
import com.performily.flowboard.features.payroll.domain.repository.PayslipRepository
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import java.time.LocalDate
import javax.inject.Inject

/** Registra el depósito de una boleta publicada (US45). */
class MarkPayslipAsPaidUseCase @Inject constructor(
    private val repository: PayslipRepository
) {

    suspend operator fun invoke(payslip: Payslip, paidOn: LocalDate): Result<Payslip> {
        if (!payslip.canUpdatePayment) {
            return Result.failure(IllegalStateException("La boleta debe estar publicada para registrar su pago."))
        }
        return runCatching { PaymentDetails.paid(paidOn) }
            .fold(
                onSuccess = { repository.markAsPaid(payslip.id, paidOn) },
                onFailure = { Result.failure(it) }
            )
    }
}
