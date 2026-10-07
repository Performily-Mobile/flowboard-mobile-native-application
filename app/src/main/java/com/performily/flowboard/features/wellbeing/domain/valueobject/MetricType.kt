package com.performily.flowboard.features.wellbeing.domain.valueobject

import java.math.BigDecimal

/**
 * Métricas ambientales que mide un dispositivo, con su unidad y su rango físico.
 * Los rangos coinciden con los que valida el backend (MetricType de Wellbeing).
 */
enum class MetricType(
    val label: String,
    val shortLabel: String,
    val unit: String,
    val physicalMin: BigDecimal,
    val physicalMax: BigDecimal
) {
    TEMPERATURE("Temperatura", "Temperatura", "°C", BigDecimal("-50"), BigDecimal("80")),
    ILLUMINATION("Iluminación", "Iluminación", "lx", BigDecimal.ZERO, BigDecimal("100000")),
    AIR_QUALITY("Calidad del aire (CO₂)", "Aire", "ppm", BigDecimal.ZERO, BigDecimal("5000"));

    fun isWithinPhysicalRange(value: BigDecimal): Boolean =
        value >= physicalMin && value <= physicalMax
}
