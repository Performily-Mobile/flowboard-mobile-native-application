package com.performily.flowboard.features.payroll.presentation.ui.components

import com.performily.flowboard.core.domain.Money
import com.performily.flowboard.features.payroll.application.usecase.PayslipEntry
import com.performily.flowboard.features.payroll.domain.valueobject.PayPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.domain.valueobject.PublicationStatus
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** "S/ 1,350.00" */
fun Money.toDisplay(): String {
    val formatted = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US)).format(amount)
    return "${currencySymbol()} $formatted"
}

/** "S/ •,•••.••": monto oculto mientras hay un diálogo encima (MA-68). */
fun Money.toMaskedDisplay(): String = "${currencySymbol()} •,•••.••"

private fun Money.currencySymbol(): String = if (currency == Money.DEFAULT_CURRENCY) "S/" else currency

/** "Neto S/ 1,420.00" */
fun netAmountLabel(amount: String): String = "Neto $amount"

/** "septiembre 2026", para usar dentro de una oración. */
val PayPeriod.inlineLabel: String get() = label.replaceFirstChar { it.lowercase() }

fun PaymentStatus.label(): String = when (this) {
    PaymentStatus.PENDING -> "Pendiente"
    PaymentStatus.PAID -> "Pagado"
    PaymentStatus.OBSERVED -> "Observado"
}

fun PublicationStatus.label(): String = when (this) {
    PublicationStatus.UNDER_REVIEW -> "Por publicar"
    PublicationStatus.PUBLISHED -> "Publicada"
}

/** "248 boletas cargadas" */
fun uploadedPayslipsLabel(count: Int): String =
    if (count == 1) "1 boleta cargada" else "$count boletas cargadas"

/** "Publicar 248 boletas" */
fun publishPayslipsLabel(count: Int): String =
    if (count == 1) "Publicar 1 boleta" else "Publicar $count boletas"

/** "3 boletas con depósito pendiente" */
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

/** "Huamán Quispe, Pedro"; si Workspace no devolvió al colaborador, "Colaborador 12". */
val PayslipEntry.listName: String
    get() = employee?.sortableName ?: "Colaborador ${payslip.employeeId.value}"

/** "Pedro Huamán Quispe" */
val PayslipEntry.fullName: String
    get() = employee?.fullName ?: "Colaborador ${payslip.employeeId.value}"

/** "PH" */
val PayslipEntry.initials: String
    get() = employee?.initials?.takeIf { it.isNotEmpty() } ?: "#"
