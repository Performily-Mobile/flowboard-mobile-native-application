package com.performily.flowboard.features.wellbeing.domain.valueobject

/**
 * Nivel de salud de una condición ambiental, del mejor al peor.
 */
enum class HealthIndicator(val severity: Int) {
    OPTIMAL(1),
    ACCEPTABLE(2),
    POOR(3),
    HAZARDOUS(4);

    fun isWorseThan(other: HealthIndicator?): Boolean = other == null || severity > other.severity

    companion object {
        /** El peor indicador de la lista, o null si no hay ninguno. */
        fun worstOf(indicators: List<HealthIndicator?>): HealthIndicator? =
            indicators.filterNotNull().maxByOrNull { it.severity }
    }
}
