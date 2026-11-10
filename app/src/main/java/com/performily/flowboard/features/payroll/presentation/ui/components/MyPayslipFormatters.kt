package com.performily.flowboard.features.payroll.presentation.ui.components

import com.performily.flowboard.features.payroll.domain.valueobject.PaymentDetails
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Texts shown in the employee payslip screens. */
object MyPayslipFormatters {

    private val SPANISH: Locale = Locale.forLanguageTag("es-PE")
    private val DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", SPANISH)
    private const val BYTES_IN_KB = 1024.0

    /**
     * Formats a date, for example "31 de agosto de 2026".
     *
     * @param date date to format.
     */
    fun date(date: LocalDate): String = DATE.format(date)

    /**
     * Summarizes the deposit in one line, for example "Depositado el 1 de septiembre de 2026".
     *
     * @param payment deposit details of the payslip.
     */
    fun paymentSummary(payment: PaymentDetails): String = when (payment.status) {
        PaymentStatus.PENDING -> "Depósito pendiente"
        PaymentStatus.PAID -> payment.paidOn?.let { "Depositado el ${date(it)}" } ?: "Depositado"
        PaymentStatus.OBSERVED -> "Depósito observado"
    }

    /**
     * Formats a file size, for example "240 KB" or "1.2 MB".
     *
     * @param bytes size of the file in bytes.
     */
    fun fileSize(bytes: Long): String {
        val kilobytes = bytes / BYTES_IN_KB
        return if (kilobytes < BYTES_IN_KB) {
            String.format(SPANISH, "%.0f KB", kilobytes.coerceAtLeast(1.0))
        } else {
            String.format(SPANISH, "%.1f MB", kilobytes / BYTES_IN_KB)
        }
    }
}
