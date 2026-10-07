package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.entity.RequestFieldValue
import com.performily.flowboard.features.request.domain.entity.RequestPerson
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.domain.entity.VacationAvailability
import com.performily.flowboard.features.request.domain.valueobject.RequestPeriod
import java.time.LocalDate
import java.time.LocalTime

enum class NewRequestStep(val number: Int, val label: String) {
    TYPE(1, "Tipo de solicitud"),
    DETAIL(2, "Detalle"),
    CONFIRMATION(3, "Confirmación")
}

data class NewRequestUiState(
    val step: NewRequestStep = NewRequestStep.TYPE,
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val types: List<RequestType> = emptyList(),
    val selectedType: RequestType? = null,
    val availability: VacationAvailability? = null,
    val approver: RequestPerson? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val byHours: Boolean = false,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val values: Map<String, String> = emptyMap(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val attachments: List<FileReference> = emptyList(),
    val isUploading: Boolean = false,
    val attachmentError: String? = null,
    val periodError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,

    val resultMessage: String? = null
) {
    val canUseHours: Boolean get() = selectedType?.deductsBalance == false

    /** Periodo armado con lo que se eligió; null si falta algo o no es válido. */
    val period: RequestPeriod?
        get() {
            val start = startDate ?: return null
            return runCatching {
                if (byHours && canUseHours) RequestPeriod(start, start, startTime, endTime)
                else RequestPeriod(start, endDate ?: return null)
            }.getOrNull()
        }

    val requestedDays: Int get() = period?.days ?: 0

    val exceedsBalance: Boolean
        get() {
            val days = requestedDays
            return selectedType?.deductsVacationDays == true && availability != null && days > 0 &&
                !availability.isEnoughFor(days)
        }

    val fieldValues: List<RequestFieldValue>
        get() = values.filterValues { it.isNotBlank() }.map { (key, value) -> RequestFieldValue(key, value) }

    val canReview: Boolean get() = period != null && !exceedsBalance && !isUploading

    val approverName: String get() = approver?.name ?: "Recursos Humanos"
}
