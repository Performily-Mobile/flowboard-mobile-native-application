package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.presentation.state.PersonalDataField
import com.performily.flowboard.features.workspace.presentation.ui.components.DatePickerField
import com.performily.flowboard.features.workspace.presentation.ui.components.FormTextField
import com.performily.flowboard.features.workspace.presentation.ui.components.SectionTitle
import com.performily.flowboard.features.workspace.presentation.viewmodel.EditPersonalDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalDataScreen(
    employeeId: Long,
    onClose: () -> Unit,
    viewModel: EditPersonalDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(employeeId) {
        viewModel.load(employeeId)
    }

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onClose()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar datos personales") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(FlowboardIcons.Close, contentDescription = "Cerrar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Button(
                    onClick = viewModel::onSave,
                    enabled = !state.isSaving && !state.isLoading,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (state.isSaving) "Guardando..." else "Guardar cambios")
                }
            }
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = state.identityDocumentLabel,
                onValueChange = {},
                enabled = false,
                label = { Text("Documento de identidad") },
                supportingText = { Text("El documento de identidad no se puede modificar.") },
                modifier = Modifier.fillMaxWidth()
            )
            FormTextField(
                label = "Nombres",
                value = state.firstName,
                onValueChange = viewModel::onFirstNameChange,
                error = state.errorOf(PersonalDataField.FIRST_NAME)
            )
            FormTextField(
                label = "Apellidos",
                value = state.lastName,
                onValueChange = viewModel::onLastNameChange,
                error = state.errorOf(PersonalDataField.LAST_NAME)
            )
            DatePickerField(
                label = "Fecha de nacimiento",
                value = state.birthDate,
                onValueChange = viewModel::onBirthDateChange,
                isError = state.errorOf(PersonalDataField.BIRTH_DATE) != null,
                supportingText = state.errorOf(PersonalDataField.BIRTH_DATE),
                modifier = Modifier.fillMaxWidth()
            )
            FormTextField(
                label = "Correo electrónico",
                value = state.email,
                onValueChange = viewModel::onEmailChange,
                error = state.errorOf(PersonalDataField.EMAIL),
                keyboardType = KeyboardType.Email
            )
            FormTextField(
                label = "Teléfono",
                value = state.phoneNumber,
                onValueChange = viewModel::onPhoneChange,
                error = state.errorOf(PersonalDataField.PHONE),
                keyboardType = KeyboardType.Phone
            )

            SectionTitle("Dirección (opcional)")
            FormTextField(label = "Calle y número", value = state.street, onValueChange = viewModel::onStreetChange)
            FormTextField(label = "Distrito", value = state.district, onValueChange = viewModel::onDistrictChange)
            FormTextField(label = "Provincia", value = state.province, onValueChange = viewModel::onProvinceChange)
            FormTextField(label = "Departamento", value = state.department, onValueChange = viewModel::onDepartmentChange)

            state.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
