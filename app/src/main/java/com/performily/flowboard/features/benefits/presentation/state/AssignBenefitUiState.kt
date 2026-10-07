package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.AreaAssignmentPreview
import com.performily.flowboard.features.benefits.domain.entity.AreaOption
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.entity.EmployeeOption
import java.time.LocalDate
import java.time.YearMonth

/** "Asignar a" del prototipo. */
enum class AssignTargetMode(val label: String) {
    EMPLOYEE("Colaborador"),
    AREA("Área completa")
}

/** MA-61 · Asignar beneficio. Por defecto la vigencia es el mes actual. */
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
    val quantityError: String? = null,
    val dateError: String? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    /** Cuando tiene valor, la asignación se hizo y la pantalla vuelve con este mensaje. */
    val resultMessage: String? = null
) {
    val hasTarget: Boolean
        get() = if (mode == AssignTargetMode.AREA) selectedArea != null else selectedEmployee != null

    /** Al asignar a un área donde todos ya lo tienen, no hay nada que asignar. */
    val nothingToAssign: Boolean
        get() = mode == AssignTargetMode.AREA && preview != null && preview.toAssign == 0

    val canSubmit: Boolean
        get() = !isSubmitting && selectedType != null && hasTarget && quantity.isNotBlank() && !nothingToAssign

    /** "Asignar a 46 colaboradores" con la vista previa del área; si no, "Asignar beneficio". */
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
