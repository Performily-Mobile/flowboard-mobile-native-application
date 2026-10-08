package com.performily.flowboard.features.payroll.presentation.ui.components

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Formats the amount with its currency symbol, for example "S/ 1,350.00".
 *
 * @receiver the amount to format
 */
fun Money.toDisplay(): String {
    val formatted = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US)).format(amount)
    return "${currencySymbol()} $formatted"
}

/**
 * Formats the amount hidden behind bullets, for example "S/ •,•••.••".
 *
 * Used while a dialog is displayed over the list (MA-68).
 *
 * @receiver the amount to hide
 */
fun Money.toMaskedDisplay(): String = "${currencySymbol()} •,•••.••"

private fun Money.currencySymbol(): String = if (currency == Money.DEFAULT_CURRENCY) "S/" else currency

/**
 * Builds the net amount label, for example "Neto S/ 1,420.00".
 *
 * @param amount the already formatted amount
 */
fun netAmountLabel(amount: String): String = "Neto $amount"

fun PaymentStatus.label(): String = when (this) {
    PaymentStatus.PENDING -> "Pendiente"
    PaymentStatus.PAID -> "Pagado"
    PaymentStatus.OBSERVED -> "Observado"
}

fun PublicationStatus.label(): String = when (this) {
    PublicationStatus.UNDER_REVIEW -> "Por publicar"
    PublicationStatus.PUBLISHED -> "Publicada"
}

/**
 * Builds the uploaded payslips counter, for example "248 boletas cargadas".
 *
 * @param count number of uploaded payslips
 */
fun uploadedPayslipsLabel(count: Int): String =
    if (count == 1) "1 boleta cargada" else "$count boletas cargadas"

/**
 * Builds the publish button label, for example "Publicar 248 boletas".
 *
 * @param count number of payslips that will be published
 */
fun publishPayslipsLabel(count: Int): String =
    if (count == 1) "Publicar 1 boleta" else "Publicar $count boletas"

/**
 * Builds the payment report counter, for example "3 boletas con depósito pendiente".
 *
 * @param count number of payslips in the report
 * @param status payment status filter, or null when no status is selected
 */
fun paymentReportCountLabel(count: Int, status: PaymentStatus?): String {
    val noun = if (count == 1) "boleta" else "boletas"
    val detail = when (status) {
        PaymentStatus.PENDING -> "con depósito pendiente"
        PaymentStatus.PAID -> "con depósito registrado"
        PaymentStatus.OBSERVED -> "con depósito observado"
        null -> if (count == 1) "publicada" else "publicadas"
    }
    return "$count $noun $detail"
}

/**
 * Name shown in lists, for example "Huamán Quispe, Pedro".
 *
 * Falls back to the employee name sent with the payslip and then to "Colaborador 12" when
 * Workspace did not return the employee.
 */
val PayslipEntry.listName: String
    get() = employee?.sortableName?.takeIf { it.isNotBlank() } ?: fullName

/**
 * Initials shown in the avatar, for example "PH", or "#" when no name is available.
 */
val PayslipEntry.initials: String
    get() = employee?.initials?.takeIf { it.isNotEmpty() }
        ?: payslip.employeeName.orEmpty().trim().split(Regex("\\s+"))
            .filter { it.isNotEmpty() }
            .take(2)
            .map { it.first() }
            .joinToString("")
            .uppercase()
            .ifEmpty { "#" }
