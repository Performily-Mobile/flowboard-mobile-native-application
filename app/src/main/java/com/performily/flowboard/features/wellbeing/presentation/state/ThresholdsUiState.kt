package com.performily.flowboard.features.wellbeing.presentation.state

import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType

/**
 * One editable row of the thresholds table.
 *
 * Values are kept as the text of the input fields.
 */
data class ThresholdRowState(
    val indicator: HealthIndicator,
    val min: String = "",
    val max: String = ""
) {
    val isBlank: Boolean get() = min.isBlank() && max.isBlank()
}

/**
 * MA-74 - Thresholds of an office.
 *
 * @property drafts rows being edited for each metric, so switching tabs does not lose typed values
 * @property configuredMetrics metrics that already have a threshold saved in the backend
 * @property saveError error of the last save attempt; it does not block the save button so the user can retry
 */
data class ThresholdsUiState(
    val officeId: Long? = null,
    val officeName: String = "",
    val selectedMetric: MetricType = MetricType.TEMPERATURE,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val drafts: Map<MetricType, List<ThresholdRowState>> = emptyMap(),
    val configuredMetrics: Set<MetricType> = emptySet(),
    val rowErrors: Map<HealthIndicator, String> = emptyMap(),
    val generalError: String? = null,
    val saveError: String? = null,
    val isSaving: Boolean = false,
    val snackbarMessage: String? = null
) {
    val rows: List<ThresholdRowState> get() = drafts[selectedMetric].orEmpty()

    val isConfigured: Boolean get() = selectedMetric in configuredMetrics

    val canSave: Boolean
        get() = !isSaving && !isLoading && rows.isNotEmpty() && rowErrors.isEmpty() && generalError == null
}
