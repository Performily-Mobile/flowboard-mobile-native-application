package com.performily.flowboard.features.benefits.presentation.state

import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.domain.valueobject.BenefitUnit
import java.time.LocalDate

/** Hoja "Nuevo tipo de beneficio" (MA-60). */
data class BenefitTypeForm(
    val name: String = "",
    val unit: BenefitUnit = BenefitUnit.MONEY,
    val hasBalance: Boolean = false,
    val nameError: String? = null,
    val isSaving: Boolean = false
)

/** Diálogo "Registrar entrega" (MA-62). */
data class DeliveryForm(
    val assignment: BenefitAssignment,
    val deliveredOn: LocalDate,
    val notes: String = "",
    val error: String? = null,
    val isSaving: Boolean = false
)

/** MA-60 / MA-62 · Beneficios de RR.HH. Pestaña 0: Catálogo; 1: Asignaciones. */
data class BenefitsAdminUiState(
    val selectedTab: Int = 0,
    val isLoadingCatalog: Boolean = false,
    val benefitTypes: List<BenefitType> = emptyList(),
    val catalogError: String? = null,
    val assignmentFilter: AssignmentStatus = AssignmentStatus.ASSIGNED,
    val isLoadingAssignments: Boolean = false,
    val assignments: List<BenefitAssignment> = emptyList(),
    val assignmentsError: String? = null,
    val isTypeSheetVisible: Boolean = false,
    val typeForm: BenefitTypeForm = BenefitTypeForm(),
    val typeToToggle: BenefitType? = null,
    val isTogglingType: Boolean = false,
    val deliveryForm: DeliveryForm? = null,
    val snackbarMessage: String? = null
)
