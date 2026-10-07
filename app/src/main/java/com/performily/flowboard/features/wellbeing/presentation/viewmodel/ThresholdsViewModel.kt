package com.performily.flowboard.features.wellbeing.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.wellbeing.application.usecase.DefineThresholdUseCase
import com.performily.flowboard.features.wellbeing.application.usecase.GetThresholdsUseCase
import com.performily.flowboard.features.wellbeing.domain.entity.MetricThreshold
import com.performily.flowboard.features.wellbeing.domain.service.ThresholdRangesValidator
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.domain.valueobject.ThresholdRange
import com.performily.flowboard.features.wellbeing.presentation.state.ThresholdRowState
import com.performily.flowboard.features.wellbeing.presentation.state.ThresholdsUiState
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingFormatters
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

/**
 * MA-74 · Configuración de umbrales (US49).
 * Valida mientras se escribe con las mismas reglas que el backend, para que el
 * error aparezca en la fila exacta y "Guardar" solo se active con rangos válidos.
 */
@HiltViewModel
class ThresholdsViewModel @Inject constructor(
    private val getThresholds: GetThresholdsUseCase,
    private val defineThreshold: DefineThresholdUseCase
) : ViewModel() {

    private val validator = ThresholdRangesValidator()

    private val _state = MutableStateFlow(ThresholdsUiState())
    val state: StateFlow<ThresholdsUiState> = _state.asStateFlow()

    fun load(officeId: Long, officeName: String) {
        if (_state.value.officeId == officeId && _state.value.drafts.isNotEmpty()) return
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

    fun onMetricSelected(metricType: MetricType) {
        _state.update { it.copy(selectedMetric = metricType) }
        validate()
    }

    fun onMinChange(indicator: HealthIndicator, value: String) = updateRow(indicator) { it.copy(min = value) }

    fun onMaxChange(indicator: HealthIndicator, value: String) = updateRow(indicator) { it.copy(max = value) }

    fun onSave() {
        val current = _state.value
        val officeId = current.officeId ?: return
        if (!current.canSave) return
        val metric = current.selectedMetric
        val ranges = parseRows(current.rows).ranges

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            defineThreshold(officeId, metric, ranges)
                .onSuccess {
                    _state.update {
                        it.copy(
                            isSaving = false,
                            configuredMetrics = it.configuredMetrics + metric,
                            snackbarMessage = "Umbrales de ${WellbeingFormatters.alertName(metric).lowercase()} guardados."
                        )
                    }
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(isSaving = false, generalError = exception.wellbeingMessage("No se pudieron guardar los umbrales."))
                    }
                }
        }
    }

    fun onSnackbarShown() = _state.update { it.copy(snackbarMessage = null) }

    private fun updateRow(indicator: HealthIndicator, change: (ThresholdRowState) -> ThresholdRowState) {
        _state.update { state ->
            val rows = state.rows.map { row -> if (row.indicator == indicator) change(row) else row }
            state.copy(drafts = state.drafts + (state.selectedMetric to rows))
        }
        validate()
    }

    /** Valida las filas de la métrica seleccionada y deja los errores en el estado. */
    private fun validate() {
        _state.update { state ->
            val parsed = parseRows(state.rows)
            val validation = validator.validate(state.selectedMetric, parsed.ranges)
            state.copy(
                // Los errores de formato de la fila tienen prioridad sobre los de rango.
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

    /** Una fila vacía no se envía (el nivel queda sin definir); a medio llenar es un error. */
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
        /** Valores sugeridos cuando la métrica aún no tiene umbral; el usuario los ajusta y guarda. */
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
