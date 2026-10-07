package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType

/** Una fila editable de la tabla de umbrales. Los valores se guardan como texto del campo. */
data class ThresholdRowState(
    val indicator: HealthIndicator,
    val min: String = "",
    val max: String = ""
) {
    val isBlank: Boolean get() = min.isBlank() && max.isBlank()
}

/** MA-74 · Umbrales de un espacio. */
data class ThresholdsUiState(
    val officeId: Long? = null,
    val officeName: String = "",
    val selectedMetric: MetricType = MetricType.TEMPERATURE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    /** Filas en edición de cada métrica: al cambiar de pestaña no se pierde lo escrito. */
    val drafts: Map<MetricType, List<ThresholdRowState>> = emptyMap(),
    /** Métricas que ya tienen umbral guardado en el backend. */
    val configuredMetrics: Set<MetricType> = emptySet(),
    val rowErrors: Map<HealthIndicator, String> = emptyMap(),
    val generalError: String? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
) {
    val rows: List<ThresholdRowState> get() = drafts[selectedMetric].orEmpty()

    val isConfigured: Boolean get() = selectedMetric in configuredMetrics

    val canSave: Boolean
        get() = !isSaving && !isLoading && rows.isNotEmpty() && rowErrors.isEmpty() && generalError == null
}
