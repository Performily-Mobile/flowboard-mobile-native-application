package com.performily.flowboard.features.wellbeing.presentation.ui.components

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Texts and formats of Wellbeing as they appear in the prototype. */
internal object WellbeingFormatters {
    private val numberSymbols = DecimalFormatSymbols(Locale.US)
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    /**
     * Formats a number with thousands separators and at most one decimal.
     *
     * A new [DecimalFormat] is created per call because the class is not thread-safe.
     * Examples: 1620 becomes "1,620" and 23.40 becomes "23.4".
     */
    fun number(value: BigDecimal): String = DecimalFormat("#,##0.#", numberSymbols).format(value)

    /** Formats a value with its unit, for example "23.4 °C" or "1,620 ppm". */
    fun value(value: BigDecimal, metricType: MetricType): String = "${number(value)} ${metricType.unit}"

    /** Formats a range with its unit, for example "20–24 °C". */
    fun range(min: BigDecimal, max: BigDecimal, metricType: MetricType): String =
        "${number(min)}–${number(max)} ${metricType.unit}"

    /** Formats a date as dd/MM/yyyy. */
    fun date(date: LocalDate): String = date.format(dateFormat)

    /** Formats a date range as "dd/MM/yyyy – dd/MM/yyyy". */
    fun dateRange(from: LocalDate, to: LocalDate): String = "${date(from)} – ${date(to)}"

    /** Formats a time as HH:mm. */
    fun time(dateTime: LocalDateTime): String = dateTime.format(timeFormat)

    /** Formats a day and time, for example "el 25/09 a las 18:02". */
    fun dayAndTime(dateTime: LocalDateTime): String =
        "el ${dateTime.format(shortDateFormat)} a las ${dateTime.format(timeFormat)}"

    /**
     * Formats the duration from a date until now, for example "2 min", "6 h" or "3 d".
     *
     * @param since start of the period
     * @param now end of the period, injectable for tests
     */
    fun elapsed(since: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(since, now).toMinutes().coerceAtLeast(0)
        return when {
            minutes < 1 -> "un momento"
            minutes < 60 -> "$minutes min"
            minutes < 60 * 24 -> "${minutes / 60} h"
            else -> "${minutes / (60 * 24)} d"
        }
    }

    /** Formats the elapsed time as a relative phrase, for example "hace 2 min". */
    fun ago(since: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String = "hace ${elapsed(since, now)}"

    /** Returns the display label of a health indicator. */
    fun indicatorLabel(indicator: HealthIndicator): String = when (indicator) {
        HealthIndicator.OPTIMAL -> "Óptimo"
        HealthIndicator.ACCEPTABLE -> "Aceptable"
        HealthIndicator.POOR -> "Deficiente"
        HealthIndicator.HAZARDOUS -> "Peligro"
    }

    /** Returns the metric name used in alerts, for example "Calidad del aire". */
    fun alertName(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura"
        MetricType.ILLUMINATION -> "Iluminación"
        MetricType.AIR_QUALITY -> "Calidad del aire"
    }

    /** Returns the lowercase level name used in alert sentences. */
    fun levelName(indicator: HealthIndicator): String = when (indicator) {
        HealthIndicator.OPTIMAL -> "óptimo"
        HealthIndicator.ACCEPTABLE -> "aceptable"
        HealthIndicator.POOR -> "deficiente"
        HealthIndicator.HAZARDOUS -> "peligroso"
    }

    /** Returns the title of the history chart, for example "CO₂ promedio diario (ppm)". */
    fun chartTitle(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura promedio diaria (°C)"
        MetricType.ILLUMINATION -> "Iluminación promedio diaria (lx)"
        MetricType.AIR_QUALITY -> "CO₂ promedio diario (ppm)"
    }

    /** Returns the subtitle of the thresholds screen, for example "Calidad del aire · CO₂ (ppm)". */
    fun thresholdTitle(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura · °C"
        MetricType.ILLUMINATION -> "Iluminación · lx"
        MetricType.AIR_QUALITY -> "Calidad del aire · CO₂ (ppm)"
    }

    /** Returns the Spanish initial of the weekday: L M M J V S D. */
    fun weekdayInitial(date: LocalDate): String = when (date.dayOfWeek.value) {
        1 -> "L"
        2 -> "M"
        3 -> "M"
        4 -> "J"
        5 -> "V"
        6 -> "S"
        else -> "D"
    }
}
