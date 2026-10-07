package com.performily.flowboard.features.wellbeing.infrastructure.mapper

import com.performily.flowboard.features.wellbeing.domain.valueobject.DeviceStatus
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDateTime

/** Convierte los textos del backend a enums y fechas sin romper la app si llega un valor desconocido. */
internal object WellbeingEnumMapper {

    fun metricType(value: String): MetricType? = MetricType.entries.firstOrNull { it.name == value }

    fun indicator(value: String?): HealthIndicator? = HealthIndicator.entries.firstOrNull { it.name == value }

    fun deviceStatus(value: String): DeviceStatus =
        DeviceStatus.entries.firstOrNull { it.name == value } ?: DeviceStatus.INACTIVE

    fun dateTime(value: String?): LocalDateTime? = value?.let { runCatching { LocalDateTime.parse(it) }.getOrNull() }
}
