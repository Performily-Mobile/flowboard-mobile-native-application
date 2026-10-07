package com.performily.flowboard.features.request.domain.valueobject

import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit

/**
 * Periodo de una solicitud. Puede ser de días completos (vacaciones) o de unas
 * horas dentro de un mismo día (permiso por horas). Mismas reglas que el backend.
 */
data class RequestPeriod(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null
) {
    init {
        require(!endDate.isBefore(startDate)) { "La fecha de fin no puede ser anterior a la de inicio." }
        require((startTime == null) == (endTime == null)) { "Indica la hora de inicio y la de fin." }
        if (startTime != null && endTime != null) {
            require(startDate == endDate) { "Un permiso por horas debe empezar y terminar el mismo día." }
            require(endTime.isAfter(startTime)) { "La hora de fin debe ser posterior a la de inicio." }
        }
    }

    val hasHours: Boolean get() = startTime != null

    /** Días calendario que se piden; es lo que el backend descuenta del saldo. */
    val days: Int get() = if (hasHours) 0 else ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

    /** Horas pedidas en un permiso por horas. */
    val hours: Double
        get() = if (startTime != null && endTime != null) Duration.between(startTime, endTime).toMinutes() / 60.0 else 0.0
}
