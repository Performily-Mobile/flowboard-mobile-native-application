package com.performily.flowboard.features.request.presentation.state

import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType

data class FieldForm(
    val label: String = "",
    val dataType: FieldDataType = FieldDataType.TEXT,
    val required: Boolean = false,
    val error: String? = null
)

data class NewRequestTypeUiState(
    val name: String = "",
    val description: String = "",
    val requiresAttachment: Boolean = false,
    val balanceDeduction: BalanceDeduction = BalanceDeduction.NONE,
    val fields: List<NewRequestField> = emptyList(),
    val fieldForm: FieldForm? = null,
    val nameError: String? = null,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    /** Cuando tiene valor, el tipo se creó y la pantalla vuelve con este mensaje. */
    val resultMessage: String? = null
) {
    val canSave: Boolean get() = !isSaving && name.isNotBlank()
}
