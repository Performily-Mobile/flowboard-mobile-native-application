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

/**
 * Benefits text formatters.
 *
 * Texts and formats exactly as they appear in the prototype (MA-57 to MA-63).
 */
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

    /**
     * Formats a plain number.
     *
     * Example: 1240 -> "1,240"; 2.50 -> "2.5".
     */
    fun number(value: BigDecimal): String = numberFormat.format(value)

    /**
     * Formats a quantity with its unit.
     *
     * Examples: "S/ 1,240.00", "1 día", "2.5 días", "1 unidad".
     */
    fun quantity(value: BigDecimal, unit: BenefitUnit): String = when (unit) {
        BenefitUnit.MONEY -> "S/ ${moneyFormat.format(value)}"
        BenefitUnit.DAYS -> "${number(value)} ${if (isOne(value)) "día" else "días"}"
        BenefitUnit.UNITS -> "${number(value)} ${if (isOne(value)) "unidad" else "unidades"}"
    }

    /**
     * Formats a signed number of days using the typographic minus sign of the prototype.
     *
     * Examples: "+2.5 días", "−3 días", "+1 día".
     */
    fun signedDays(value: BigDecimal): String {
        val sign = if (value.signum() < 0) "−" else "+"
        val abs = value.abs()
        return "$sign${number(abs)} ${if (isOne(abs)) "día" else "días"}"
    }

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun shortDate(date: LocalDate): String = date.format(shortDateFormat)

    /**
     * Formats a date and time for display.
     *
     * Example: "el 26/09 a las 08:10".
     */
    fun syncedAt(dateTime: LocalDateTime): String =
        "el ${dateTime.format(shortDateFormat)} a las ${dateTime.format(timeFormat)}"

    /**
     * Formats the validity period of an assignment.
     *
     * Shows "diciembre 2026" for a single month, otherwise "01/10/2026 – 15/11/2026".
     */
    fun period(start: LocalDate, end: LocalDate): String =
        if (start.year == end.year && start.month == end.month) {
            "${monthNames[start.monthValue - 1]} ${start.year}"
        } else {
            "${date(start)} – ${date(end)}"
        }

    /**
     * Formats a request code as the Request context does.
     *
     * Example: 142 -> "S-0142".
     */
    fun requestCode(requestId: Long): String = "S-%04d".format(requestId)

    /**
     * Builds avatar initials from a full name.
     *
     * Example: "María Quispe Rojas" -> "MQ".
     */
    fun initials(name: String): String =
        name.split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }

    /**
     * Builds the title of a vacation movement (MA-57, MA-63).
     */
    fun movementTitle(movement: VacationMovement): String = when (movement.type) {
        VacationMovementType.ACCRUAL -> movement.reason ?: "Acumulación"
        VacationMovementType.USAGE -> movement.requestId?.let { "Uso · Solicitud ${requestCode(it)}" } ?: "Uso de vacaciones"
        VacationMovementType.REVERSAL -> movement.requestId?.let { "Reversión · Solicitud ${requestCode(it)}" } ?: "Reversión"
        VacationMovementType.MANUAL_ADJUSTMENT -> "Ajuste manual"
    }

    /**
     * Builds the detail line under a movement title.
     *
     * Shows the date and, for an adjustment, who made it and the reason.
     */
    fun movementDetail(movement: VacationMovement): String {
        val day = movement.occurredAt.toLocalDate()
        return when (movement.type) {
            VacationMovementType.MANUAL_ADJUSTMENT -> listOfNotNull(
                shortDate(day),
                movement.authorName?.let { "$it (RR.HH.)" },
                movement.reason?.let { "Motivo: $it" }
            ).joinToString(" · ")
            VacationMovementType.REVERSAL -> "${shortDate(day)} · Solicitud aprobada anulada"
            else -> date(day)
        }
    }

    private fun isOne(value: BigDecimal): Boolean = value.compareTo(BigDecimal.ONE) == 0
}
