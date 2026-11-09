package com.performily.flowboard.features.dashboard.presentation.ui.components

import com.performily.flowboard.features.dashboard.domain.entity.MyRequest
import com.performily.flowboard.features.dashboard.domain.entity.TodayAttendance
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Text shown in the "Hoy" card of the employee dashboard.
 *
 * @property title main line of the card.
 * @property detail secondary line of the card.
 * @property action label of the button of the card.
 */
internal data class TodayCardText(
    val title: String,
    val detail: String,
    val action: String
)

/** Text formatting helpers for the employee dashboard. */
internal object EmployeeDashboardFormatters {

    private val shortDate = DateTimeFormatter.ofPattern("dd/MM")
    private val shortDateTime = DateTimeFormatter.ofPattern("dd/MM HH:mm")
    private val clock = DateTimeFormatter.ofPattern("HH:mm")
    private val longDay = DateTimeFormatter.ofPattern("EEEE dd/MM", Locale.forLanguageTag("es-PE"))

    /** Formats a percentage, or a dash when it is not known. */
    fun percentage(value: Int?): String = value?.let { "$it%" } ?: "—"

    /** Formats a number of days without useless decimals, or a dash when it is not known. */
    fun days(value: BigDecimal?): String = value?.stripTrailingZeros()?.toPlainString() ?: "—"

    /** Formats a number of hours with one decimal, for example "128.5 h". */
    fun hours(value: Double): String = String.format(Locale.US, "%.1f h", value)

    /** Formats a time of the day, for example "08:02". */
    fun time(value: LocalTime): String = value.format(clock)

    /** Formats a day for the card header, for example "Lunes 18/10". */
    fun longDay(value: LocalDate): String = value.format(longDay).replaceFirstChar { it.uppercase() }

    /** Formats the moment the vacation balance was saved, for example "18/10 08:30". */
    fun syncedAt(value: LocalDateTime): String = value.format(shortDateTime)

    /** Formats the expiry of a benefit, for example "Vence el 30/11". */
    fun expiry(value: LocalDate): String = "Vence el ${value.format(shortDate)}"

    /**
     * Describes a request, for example "Vacaciones · 19/10 al 23/10" or "Permiso por horas · 19/10 · 2 h".
     */
    fun requestDetail(request: MyRequest): String {
        val start = request.startDate
        val end = request.endDate
        val period = when {
            start == null -> null
            request.hours != null -> "${start.format(shortDate)} · ${requestHours(request.hours)}"
            end == null || end == start -> start.format(shortDate)
            else -> "${start.format(shortDate)} al ${end.format(shortDate)}"
        }
        return listOfNotNull(request.typeName, period).joinToString(" · ")
    }

    /**
     * Chooses the texts of the "Hoy" card from today's attendance.
     *
     * @param today attendance of the day, or null when it could not be loaded.
     */
    fun todayCard(today: TodayAttendance?): TodayCardText {
        val record = today?.record
        return when {
            today == null -> TodayCardText(
                title = "No se pudo cargar tu asistencia",
                detail = "Desliza hacia abajo para intentarlo de nuevo.",
                action = "Ver mi asistencia"
            )
            record == null -> TodayCardText(
                title = "Aún no marcas tu entrada",
                detail = "Tu jornada empieza cuando registres la entrada.",
                action = "Marcar entrada"
            )
            record.checkIn != null && record.checkOut == null -> TodayCardText(
                title = "Entrada a las ${time(record.checkIn)}",
                detail = "Recuerda marcar tu salida al terminar.",
                action = "Marcar salida"
            )
            record.checkIn != null && record.checkOut != null -> TodayCardText(
                title = "Jornada completada",
                detail = "${time(record.checkIn)} – ${time(record.checkOut)}" +
                    (record.effectiveHours?.let { " · ${hours(it)}" } ?: ""),
                action = "Ver mi asistencia"
            )
            else -> TodayCardText(
                title = record.status.label,
                detail = "Revisa el detalle en tu asistencia.",
                action = "Ver mi asistencia"
            )
        }
    }

    private fun requestHours(value: Double): String =
        if (value % 1.0 == 0.0) "${value.toInt()} h" else String.format(Locale.US, "%.1f h", value)
}
