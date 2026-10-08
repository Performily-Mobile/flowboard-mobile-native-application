package com.performily.flowboard.features.dashboard.presentation.ui.components

import com.performily.flowboard.features.dashboard.domain.entity.RequestToAttend
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Text formatting helpers for the dashboard. */
internal object DashboardFormatters {

    private val shortDate = DateTimeFormatter.ofPattern("dd/MM")

    /**
     * Builds the subtitle of the top bar, for example "Buenos días, Ana · actualizado hace 4 min".
     *
     * @param firstName first name of the user, if known.
     * @param loadedAt moment the dashboard was loaded, if it was.
     * @param now current moment, used for the greeting and the elapsed time.
     */
    fun subtitle(firstName: String?, loadedAt: LocalDateTime?, now: LocalDateTime): String {
        val greeting = greeting(now.toLocalTime()) + (firstName?.let { ", $it" } ?: "")
        return loadedAt?.let { "$greeting · ${updatedAgo(it, now)}" } ?: greeting
    }

    /** Formats a counter, or a dash when the section could not be loaded. */
    fun count(value: Number?): String = value?.toString() ?: "—"

    /** Formats a lateness percentage, for example "2.8% de las marcaciones". */
    fun lateness(percentage: Double): String =
        String.format(Locale.US, "%.1f%% de las marcaciones", percentage)

    /**
     * Describes a pending request, for example "Vacaciones · 19/10 al 23/10"
     * or "Descanso médico · sin jefe asignado".
     */
    fun requestDetail(request: RequestToAttend): String {
        val start = request.startDate
        val end = request.endDate
        val period = when {
            request.withoutDirectManager -> "sin jefe asignado"
            start == null -> null
            request.hours != null -> "${start.format(shortDate)} · ${hours(request.hours)}"
            end == null || end == start -> "${start.format(shortDate)} · 1 día"
            else -> "${start.format(shortDate)} al ${end.format(shortDate)}"
        }
        return listOfNotNull(request.requestTypeName, period).joinToString(" · ")
    }

    /** Returns the uppercase initials of the first two words of a name. */
    fun initials(name: String): String =
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

    private fun greeting(time: LocalTime): String = when {
        time.hour < 12 -> "Buenos días"
        time.hour < 19 -> "Buenas tardes"
        else -> "Buenas noches"
    }

    private fun updatedAgo(loadedAt: LocalDateTime, now: LocalDateTime): String {
        val minutes = Duration.between(loadedAt, now).toMinutes()
        return when {
            minutes < 1 -> "actualizado ahora"
            minutes < 60 -> "actualizado hace $minutes min"
            else -> "actualizado hace ${minutes / 60} h"
        }
    }

    private fun hours(value: Double): String =
        if (value % 1.0 == 0.0) "${value.toInt()} h" else String.format(Locale.US, "%.1f h", value)
}
