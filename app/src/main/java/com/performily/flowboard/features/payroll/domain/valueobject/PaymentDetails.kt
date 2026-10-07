package com.performily.flowboard.features.payroll.domain.valueobject

import java.time.LocalDate

/**
 * Deposit status of a payslip.
 *
 * Rules: PENDING has no date or reason; PAID has a date; OBSERVED has a reason of up to 500 characters.
 * The deposit date must not be in the future only when the payment is registered with [paid]; it is not
 * required again when the status is rebuilt from server data.
 *
 * @property status the deposit status
 * @property paidOn the deposit date, only when PAID
 * @property observationReason the reason of the incident, only when OBSERVED
 */
data class PaymentDetails(
    val status: PaymentStatus,
    val paidOn: LocalDate? = null,
    val observationReason: String? = null
) {
    init {
        when (status) {
            PaymentStatus.PENDING -> require(paidOn == null && observationReason == null) {
                "Un pago pendiente no tiene fecha ni motivo."
            }
            PaymentStatus.PAID -> require(paidOn != null) { "Un pago realizado requiere fecha." }
            PaymentStatus.OBSERVED -> require(
                !observationReason.isNullOrBlank() && observationReason.trim().length <= REASON_MAX_LENGTH
            ) { "La observación requiere un motivo de hasta $REASON_MAX_LENGTH caracteres." }
        }
    }

    companion object {
        const val REASON_MAX_LENGTH = 500

        fun pending(): PaymentDetails = PaymentDetails(PaymentStatus.PENDING)

        fun paid(paidOn: LocalDate): PaymentDetails {
            require(!paidOn.isAfter(LocalDate.now())) { "La fecha de depósito no puede ser futura." }
            return PaymentDetails(PaymentStatus.PAID, paidOn = paidOn)
        }

        fun observed(reason: String): PaymentDetails =
            PaymentDetails(PaymentStatus.OBSERVED, observationReason = reason.trim())
    }
}
