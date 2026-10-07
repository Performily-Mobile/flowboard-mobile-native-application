package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.DefineThresholdUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.GetThresholdsUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.ValidateThresholdRangesUseCase
import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import com.performily.flowboard.features.wellbeing.presentation.state.ThresholdRowState
import com.performily.flowboard.features.wellbeing.presentation.state.ThresholdsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * MA-74 - Threshold configuration (US49).
 *
 * Validates while the user types with the same rules as the backend, so the error appears on
 * the exact row and "Guardar" is enabled only with valid ranges.
 */
@HiltViewModel
class ThresholdsViewModel @Inject constructor(
    private val getThresholds: GetThresholdsUseCase,
    private val defineThreshold: DefineThresholdUseCase,
    private val validateRanges: ValidateThresholdRangesUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ThresholdsUiState())
    val state: StateFlow<ThresholdsUiState> = _state.asStateFlow()

    /**
     * Loads the saved thresholds and builds the editable drafts.
     *
     * Does nothing when the drafts of this office are already loaded or a load is running.
     *
     * @param officeId office to configure
     * @param officeName name shown in the top bar
     */
    fun load(officeId: Long, officeName: String) {
        val current = _state.value
        if (current.officeId == officeId && (current.drafts.isNotEmpty() || current.isLoading)) return
        _state.update { ThresholdsUiState(officeId = officeId, officeName = officeName, isLoading = true) }
        viewModelScope.launch {
            getThresholds(officeId)
                .onSuccess { thresholds ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            drafts = buildDrafts(thresholds),
                            configuredMetrics = thresholds.map { threshold -> threshold.metricType }.toSet()
                        )
                    }
                    validate()
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isLoading = false, errorMessage = exception.wellbeingMessage("No se pudieron cargar los umbrales."))
                    }
                }
        }
    }

    /** Reloads after a failed load, when the user taps "Reintentar". */
    fun retry() {
        val current = _state.value
        val officeId = current.officeId ?: return
        load(officeId, current.officeName)
    }

    fun onMetricSelected(metricType: MetricType) {
        _state.update { it.copy(selectedMetric = metricType, saveError = null) }
        validate()
    }

    fun onMinChange(indicator: HealthIndicator, value: String) = updateRow(indicator) { it.copy(min = value) }

    fun onMaxChange(indicator: HealthIndicator, value: String) = updateRow(indicator) { it.copy(max = value) }

    /**
     * Saves the ranges of the selected metric.
     *
     * A failure is reported in [ThresholdsUiState.saveError], which does not disable the save
     * button, so the user can retry without editing a field.
     */
    fun onSave() {
        val current = _state.value
        val officeId = current.officeId ?: return
        if (!current.canSave) return
        val metric = current.selectedMetric
        val ranges = parseRows(current.rows).ranges

        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            defineThreshold(officeId, metric, ranges)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            configuredMetrics = it.configuredMetrics + metric,
                            snackbarMessage = "Umbrales de ${metric.shortLabel.lowercase()} guardados."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSaving = false, saveError = exception.wellbeingMessage("No se pudieron guardar los umbrales."))
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private fun updateRow(indicator: HealthIndicator, change: (ThresholdRowState) -> ThresholdRowState) {
        _state.update { state ->
            val rows = state.rows.map { row -> if (row.indicator == indicator) change(row) else row }
            state.copy(drafts = state.drafts + (state.selectedMetric to rows), saveError = null)
        }
        validate()
    }

    /**
     * Validates the rows of the selected metric and stores the errors in the state.
     *
     * Format errors of a row take priority over range errors.
     */
    private fun validate() {
        _state.update { state ->
            val parsed = parseRows(state.rows)
            val validation = validateRanges(state.selectedMetric, parsed.ranges)
            state.copy(
                rowErrors = validation.rowErrors + parsed.errors,
                generalError = validation.generalError
            )
        }
    }

    private fun buildDrafts(thresholds: List<MetricThreshold>): Map<MetricType, List<ThresholdRowState>> =
        MetricType.entries.associateWith { metric ->
            val saved = thresholds.firstOrNull { it.metricType == metric }
            val ranges = saved?.ranges ?: defaultRanges(metric)
            HealthIndicator.entries.map { indicator ->
                val range = ranges.firstOrNull { it.indicator == indicator }
                ThresholdRowState(
                    indicator = indicator,
                    min = range?.minValue?.plain().orEmpty(),
                    max = range?.maxValue?.plain().orEmpty()
                )
            }
        }

    private data class ParsedRows(val ranges: List<ThresholdRange>, val errors: Map<HealthIndicator, String>)

    /**
     * Converts the text rows into ranges.
     *
     * A blank row is not sent (the level stays undefined); a half-filled or non-numeric row is an error.
     */
    private fun parseRows(rows: List<ThresholdRowState>): ParsedRows {
        val ranges = mutableListOf<ThresholdRange>()
        val errors = mutableMapOf<HealthIndicator, String>()
        rows.filterNot { it.isBlank }.forEach { row ->
            val min = row.min.trim().toBigDecimalOrNull()
            val max = row.max.trim().toBigDecimalOrNull()
            when {
                row.min.isBlank() || row.max.isBlank() -> errors[row.indicator] = "Completa mín. y máx."
                min == null || max == null -> errors[row.indicator] = "Ingresa un número válido."
                else -> ranges += ThresholdRange(row.indicator, min, max)
            }
        }
        return ParsedRows(ranges, errors)
    }

    private fun BigDecimal.plain(): String = stripTrailingZeros().toPlainString()

    companion object {
        /**
         * Suggested values used when a metric has no threshold yet; the user adjusts and saves them.
         *
         * @param metric metric to suggest ranges for
         */
        fun defaultRanges(metric: MetricType): List<ThresholdRange> = when (metric) {
            MetricType.TEMPERATURE -> listOf(
                range(HealthIndicator.OPTIMAL, 18, 24),
                range(HealthIndicator.ACCEPTABLE, 24, 27),
                range(HealthIndicator.POOR, 27, 30),
                range(HealthIndicator.HAZARDOUS, 30, 80)
            )
            MetricType.ILLUMINATION -> listOf(
                range(HealthIndicator.HAZARDOUS, 0, 150),
                range(HealthIndicator.POOR, 150, 300),
                range(HealthIndicator.ACCEPTABLE, 300, 500),
                range(HealthIndicator.OPTIMAL, 500, 100000)
            )
            MetricType.AIR_QUALITY -> listOf(
                range(HealthIndicator.OPTIMAL, 0, 600),
                range(HealthIndicator.ACCEPTABLE, 600, 1000),
                range(HealthIndicator.POOR, 1000, 1500),
                range(HealthIndicator.HAZARDOUS, 1500, 5000)
            )
        }

        private fun range(indicator: HealthIndicator, min: Int, max: Int) =
            ThresholdRange(indicator, BigDecimal(min), BigDecimal(max))
    }
}
