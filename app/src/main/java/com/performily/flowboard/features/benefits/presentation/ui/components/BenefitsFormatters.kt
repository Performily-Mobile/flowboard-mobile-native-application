package com.performily.flowboard.features.benefits.presentation.ui.components

import com.performily.flowboard.features.benefits.domain.entity.VacationMovement
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import com.performily.flowboard.features.benefits.domain.valueobject.VacationMovementType
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Textos y formatos de Benefits tal como aparecen en el prototipo (MA-57 a MA-63). */
internal object BenefitsFormatters {
    private val symbols = DecimalFormatSymbols(Locale.US)
    private val moneyFormat = DecimalFormat("#,##0.00", symbols)
    private val numberFormat = DecimalFormat("#,##0.##", symbols)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val monthNames = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    /** 1240 -> "1,240"; 2.50 -> "2.5" */
    fun number(value: BigDecimal): String = numberFormat.format(value)

    /** "S/ 1,240.00", "1 día", "2.5 días", "1 unidad" */
    fun quantity(value: BigDecimal, unit: BenefitUnit): String = when (unit) {
        BenefitUnit.MONEY -> "S/ ${moneyFormat.format(value)}"
        BenefitUnit.DAYS -> "${number(value)} ${if (isOne(value)) "día" else "días"}"
        BenefitUnit.UNITS -> "${number(value)} ${if (isOne(value)) "unidad" else "unidades"}"
    }

    /** "+2.5 días", "−3 días", "+1 día" (con el signo menos tipográfico del prototipo). */
    fun signedDays(value: BigDecimal): String {
        val sign = if (value.signum() < 0) "−" else "+"
        val abs = value.abs()
        return "$sign${number(abs)} ${if (isOne(abs)) "día" else "días"}"
    }

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun shortDate(date: LocalDate): String = date.format(shortDateFormat)

    /** "el 26/09 a las 08:10" */
    fun syncedAt(dateTime: LocalDateTime): String =
        "el ${dateTime.format(shortDateFormat)} a las ${dateTime.format(timeFormat)}"

    /** Periodo de una asignación: "diciembre 2026" si es un solo mes, si no "01/10/2026 – 15/11/2026". */
    fun period(start: LocalDate, end: LocalDate): String =
        if (start.year == end.year && start.month == end.month) {
            "${monthNames[start.monthValue - 1]} ${start.year}"
        } else {
            "${date(start)} – ${date(end)}"
        }

    /** Código de una solicitud como en Request: 142 -> "S-0142". */
    fun requestCode(requestId: Long): String = "S-%04d".format(requestId)

    /** "María Quispe Rojas" -> "MQ" */
    fun initials(name: String): String =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    /** Título de un movimiento de vacaciones (MA-57, MA-63). */
    fun movementTitle(movement: VacationMovement): String = when (movement.type) {
        VacationMovementType.ACCRUAL -> movement.reason ?: "Acumulación"
        VacationMovementType.USAGE -> movement.requestId?.let { "Uso · Solicitud ${requestCode(it)}" } ?: "Uso de vacaciones"
        VacationMovementType.REVERSAL -> movement.requestId?.let { "Reversión · Solicitud ${requestCode(it)}" } ?: "Reversión"
        VacationMovementType.MANUAL_ADJUSTMENT -> "Ajuste manual"
    }

    /** Detalle bajo el título: fecha, y para un ajuste quién lo hizo y el motivo. */
    fun movementDetail(movement: VacationMovement): String {
        val date = movement.occurredAt.toLocalDate()
        return when (movement.type) {
            VacationMovementType.MANUAL_ADJUSTMENT -> listOfNotNull(
                shortDate(date),
                movement.authorName?.let { "$it (RR.HH.)" },
                movement.reason?.let { "Motivo: $it" }
            ).joinToString(" · ")
            VacationMovementType.REVERSAL -> "${shortDate(date)} · Solicitud aprobada anulada"
            else -> date(date)
        }
    }

    private fun isOne(value: BigDecimal): Boolean = value.compareTo(BigDecimal.ONE) == 0
}
