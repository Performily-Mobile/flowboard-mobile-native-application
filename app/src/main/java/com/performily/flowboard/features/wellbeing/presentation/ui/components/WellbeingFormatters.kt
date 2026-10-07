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

/** Textos y formatos de Wellbeing tal como aparecen en el prototipo. */
internal object WellbeingFormatters {
    private val numberFormat = DecimalFormat("#,##0.#", DecimalFormatSymbols(Locale.US))
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val shortDateFormat = DateTimeFormatter.ofPattern("dd/MM")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    /** 1620 -> "1,620"; 23.40 -> "23.4" */
    fun number(value: BigDecimal): String = numberFormat.format(value)

    /** "23.4 °C", "1,620 ppm" */
    fun value(value: BigDecimal, metricType: MetricType): String = "${number(value)} ${metricType.unit}"

    /** "20–24 °C" */
    fun range(min: BigDecimal, max: BigDecimal, metricType: MetricType): String =
        "${number(min)}–${number(max)} ${metricType.unit}"

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun dateRange(from: LocalDate, to: LocalDate): String = "${date(from)} – ${date(to)}"

    fun time(dateTime: LocalDateTime): String = dateTime.format(timeFormat)

    /** "el 25/09 a las 18:02" */
    fun dayAndTime(dateTime: LocalDateTime): String =
        "el ${dateTime.format(shortDateFormat)} a las ${dateTime.format(timeFormat)}"

    /** Duración desde una fecha hasta ahora: "2 min", "6 h", "3 d". */
    fun elapsed(since: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String {
        val minutes = Duration.between(since, now).toMinutes().coerceAtLeast(0)
        return when {
            minutes < 1 -> "un momento"
            minutes < 60 -> "$minutes min"
            minutes < 60 * 24 -> "${minutes / 60} h"
            else -> "${minutes / (60 * 24)} d"
        }
    }

    /** "hace 2 min" */
    fun ago(since: LocalDateTime, now: LocalDateTime = LocalDateTime.now()): String = "hace ${elapsed(since, now)}"

    fun indicatorLabel(indicator: HealthIndicator): String = when (indicator) {
        HealthIndicator.OPTIMAL -> "Óptimo"
        HealthIndicator.ACCEPTABLE -> "Aceptable"
        HealthIndicator.POOR -> "Deficiente"
        HealthIndicator.HAZARDOUS -> "Peligro"
    }

    /** Nombre de la métrica en los avisos: "Calidad del aire en nivel peligroso". */
    fun alertName(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura"
        MetricType.ILLUMINATION -> "Iluminación"
        MetricType.AIR_QUALITY -> "Calidad del aire"
    }

    fun levelName(indicator: HealthIndicator): String = when (indicator) {
        HealthIndicator.OPTIMAL -> "óptimo"
        HealthIndicator.ACCEPTABLE -> "aceptable"
        HealthIndicator.POOR -> "deficiente"
        HealthIndicator.HAZARDOUS -> "peligroso"
    }

    /** Título del gráfico del histórico: "CO₂ promedio diario (ppm)". */
    fun chartTitle(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura promedio diaria (°C)"
        MetricType.ILLUMINATION -> "Iluminación promedio diaria (lx)"
        MetricType.AIR_QUALITY -> "CO₂ promedio diario (ppm)"
    }

    /** Subtítulo del umbral en la pantalla de umbrales: "Calidad del aire · CO₂ (ppm)". */
    fun thresholdTitle(metricType: MetricType): String = when (metricType) {
        MetricType.TEMPERATURE -> "Temperatura · °C"
        MetricType.ILLUMINATION -> "Iluminación · lx"
        MetricType.AIR_QUALITY -> "Calidad del aire · CO₂ (ppm)"
    }

    /** Inicial del día de la semana en español: L M M J V S D. */
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
