package com.performily.flowboard.features.workspace.presentation.state

import java.time.LocalDate

enum class PersonalDataField {
    FIRST_NAME,
    LAST_NAME,
    BIRTH_DATE,
    EMAIL,
    PHONE,
    ADDRESS
}

data class EditPersonalDataUiState(
    val isLoading: Boolean = false,
    val identityDocumentLabel: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val birthDate: LocalDate? = null,
    val email: String = "",
    val phoneNumber: String = "",
    val street: String = "",
    val district: String = "",
    val province: String = "",
    val department: String = "",
    val fieldErrors: Map<PersonalDataField, String> = emptyMap(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
) {
    fun errorOf(field: PersonalDataField): String? = fieldErrors[field]
}
