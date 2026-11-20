package com.performily.flowboard.features.notifications.presentation.ui.components

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

/** Texts of the notification screen. */
object NotificationFormatters {

    private val SPANISH: Locale = Locale.forLanguageTag("es-PE")
    private const val MINUTES_IN_HOUR = 60L
    private const val DAYS_IN_WEEK = 7L

    /**
     * Moment of a notice relative to now, for example "Hace 5 min", "Hace 2 h", "Ayer" or "12 oct".
     * Notices that only know their day (midnight) show "Hoy" instead of the hours since midnight.
     *
     * @param at moment of the notice.
     * @param now current moment.
     */
    fun relativeTime(at: LocalDateTime, now: LocalDateTime): String {
        val day = at.toLocalDate()
        val today = now.toLocalDate()
        val minutes = Duration.between(at, now).toMinutes().coerceAtLeast(0)
        return when {
            day == today && at.toLocalTime() == LocalTime.MIDNIGHT -> "Hoy"
            day == today && minutes < 1 -> "Ahora"
            day == today && minutes < MINUTES_IN_HOUR -> "Hace $minutes min"
            day == today -> "Hace ${minutes / MINUTES_IN_HOUR} h"
            day == today.minusDays(1) -> "Ayer"
            else -> shortDay(day)
        }
    }

    /**
     * Day and short month in lower case and without the period, for example "15 nov".
     *
     * @param day day to format.
     * @return the formatted day.
     */
    private fun shortDay(day: LocalDate): String =
        "${day.dayOfMonth} ${day.month.getDisplayName(TextStyle.SHORT, SPANISH).replace(".", "").lowercase(SPANISH)}"

    /**
     * Group a notice belongs to: "Hoy", "Ayer", "Esta semana" or "Anteriores".
     *
     * @param day day of the notice.
     * @param today current date.
     */
    fun group(day: LocalDate, today: LocalDate): String = when {
        !day.isBefore(today) -> "Hoy"
        day == today.minusDays(1) -> "Ayer"
        day.isAfter(today.minusDays(DAYS_IN_WEEK)) -> "Esta semana"
        else -> "Anteriores"
    }
}
