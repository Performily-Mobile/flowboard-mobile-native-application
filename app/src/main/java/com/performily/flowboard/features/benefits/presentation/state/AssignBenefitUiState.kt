package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption
import java.time.LocalDate
import java.time.YearMonth

/**
 * Assignment target mode.
 *
 * Options of the "Asignar a" selector: a single employee or a whole area.
 */
enum class AssignTargetMode(val label: String) {
    EMPLOYEE("Colaborador"),
    AREA("Área completa")
}

/**
 * UI state of the assign benefit screen (MA-61).
 *
 * The validity period defaults to the current month.
 */
data class AssignBenefitUiState(
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val benefitTypes: List<BenefitType> = emptyList(),
    val areas: List<AreaOption> = emptyList(),
    val employees: List<EmployeeOption> = emptyList(),
    val selectedType: BenefitType? = null,
    val mode: AssignTargetMode = AssignTargetMode.EMPLOYEE,
    val selectedArea: AreaOption? = null,
    val selectedEmployee: EmployeeOption? = null,
    val quantity: String = "",
    val startDate: LocalDate = YearMonth.now().atDay(1),
    val endDate: LocalDate = YearMonth.now().atEndOfMonth(),
    val preview: AreaAssignmentPreview? = null,
    val isLoadingPreview: Boolean = false,
    /**
     * Area preview failure message.
     *
     * While set, assigning to an area is blocked until the preview is computed successfully.
     */
    val previewError: String? = null,
    val quantityError: String? = null,
    val dateError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    /**
     * Success message after assigning.
     *
     * When set, the assignment was made and the screen returns with this message.
     */
    val resultMessage: String? = null
) {
    val hasTarget: Boolean
        get() = if (mode == AssignTargetMode.AREA) selectedArea != null else selectedEmployee != null

    /**
     * Whether there is nothing to assign.
     *
     * True when assigning to an area where every employee already has the benefit.
     */
    val nothingToAssign: Boolean
        get() = mode == AssignTargetMode.AREA && preview != null && preview.toAssign == 0

    val canSubmit: Boolean
        get() = !isSubmitting && selectedType != null && hasTarget && quantity.isNotBlank() && !nothingToAssign &&
            !(mode == AssignTargetMode.AREA && previewError != null)

    /**
     * Submit button label.
     *
     * Shows "Asignar a 46 colaboradores" when the area preview is available, otherwise "Asignar beneficio".
     */
    val submitLabel: String
        get() {
            val count = preview?.toAssign
            return if (mode == AssignTargetMode.AREA && count != null && count > 0) {
                "Asignar a $count ${if (count == 1) "colaborador" else "colaboradores"}"
            } else {
                "Asignar beneficio"
            }
        }
}
