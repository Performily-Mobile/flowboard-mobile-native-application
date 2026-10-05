package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocumentType
import com.performily.flowboard.features.workspace.presentation.state.RegisterEmployeeUiState
import com.performily.flowboard.features.workspace.presentation.state.RegisterField
import java.time.LocalDate

@Composable
fun PersonalDataStep(
    state: RegisterEmployeeUiState,
    onFirstNameChange: (String) -> Unit,
    onLastNameChange: (String) -> Unit,
    onDocumentTypeChange: (IdentityDocumentType) -> Unit,
    onDocumentNumberChange: (String) -> Unit,
    onBirthDateChange: (LocalDate) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FormTextField(
            label = "Nombres",
            value = state.firstName,
            onValueChange = onFirstNameChange,
            error = state.errorOf(RegisterField.FIRST_NAME)
        )
        FormTextField(
            label = "Apellidos",
            value = state.lastName,
            onValueChange = onLastNameChange,
            error = state.errorOf(RegisterField.LAST_NAME)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SelectField(
                label = "Tipo",
                options = IdentityDocumentType.entries,
                selected = state.documentType,
                optionLabel = { it.label() },
                onSelect = onDocumentTypeChange,
                modifier = Modifier.width(120.dp)
            )
            FormTextField(
                label = "Número de documento",
                value = state.documentNumber,
                onValueChange = onDocumentNumberChange,
                error = state.errorOf(RegisterField.DOCUMENT_NUMBER),
                keyboardType = if (state.documentType == IdentityDocumentType.DNI) KeyboardType.Number else KeyboardType.Text,
                modifier = Modifier.weight(1f)
            )
        }
        DatePickerField(
            label = "Fecha de nacimiento",
            value = state.birthDate,
            onValueChange = onBirthDateChange,
            isError = state.errorOf(RegisterField.BIRTH_DATE) != null,
            supportingText = state.errorOf(RegisterField.BIRTH_DATE),
            modifier = Modifier.fillMaxWidth()
        )
        FormTextField(
            label = "Correo corporativo",
            value = state.email,
            onValueChange = onEmailChange,
            error = state.errorOf(RegisterField.EMAIL),
            keyboardType = KeyboardType.Email
        )
        FormTextField(
            label = "Teléfono",
            value = state.phoneNumber,
            onValueChange = onPhoneChange,
            error = state.errorOf(RegisterField.PHONE),
            keyboardType = KeyboardType.Phone
        )
    }
}

@Composable
fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    error: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = singleLine,
        minLines = minLines,
        modifier = modifier
    )
}
