package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import java.time.LocalDate

/** Por defecto el histórico muestra los últimos 7 días, incluido hoy. */
private const val DEFAULT_DAYS = 6L

/** MA-75 · Histórico de una métrica. */
data class ReadingHistoryUiState(
    val officeId: Long? = null,
    val officeName: String = "",
    val selectedMetric: MetricType = MetricType.AIR_QUALITY,
    val from: LocalDate = LocalDate.now().minusDays(DEFAULT_DAYS),
    val to: LocalDate = LocalDate.now(),
    val isLoading: Boolean = false,
    val history: ReadingHistory? = null,
    val errorMessage: String? = null
)

/**
 * Frase de resumen bajo el gráfico. Distingue un problema persistente (3 días o más
 * por encima del nivel aceptable) de un episodio puntual, como pide la US51.
 */
fun ReadingHistory.summary(): String {
    val hasThreshold = dailyAverages.any { it.indicator != null }
    return when {
        !hasThreshold ->
            "Configura los umbrales de esta métrica para identificar los días fuera de rango."
        isPersistentProblem ->
            "Problema persistente: $daysAboveAcceptable días por encima del nivel aceptable en el período."
        daysAboveAcceptable > 0 ->
            "Episodio puntual: $daysAboveAcceptable ${if (daysAboveAcceptable == 1) "día" else "días"} por encima del nivel aceptable."
        else ->
            "Ningún día superó el nivel aceptable en el período."
    }
}
