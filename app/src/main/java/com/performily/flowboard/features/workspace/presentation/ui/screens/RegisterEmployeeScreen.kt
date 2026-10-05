package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.presentation.ui.components.ContractAssignmentStep
import com.performily.flowboard.features.workspace.presentation.ui.components.PersonalDataStep
import com.performily.flowboard.features.workspace.presentation.viewmodel.RegisterEmployeeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterEmployeeScreen(
    onClose: () -> Unit,
    onRegistered: (Long) -> Unit,
    viewModel: RegisterEmployeeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.registeredEmployeeId) {
        state.registeredEmployeeId?.let(onRegistered)
    }

    BackHandler(enabled = state.step == 2) { viewModel.onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo colaborador") },
                navigationIcon = {
                    if (state.step == 1) {
                        IconButton(onClick = onClose) {
                            Icon(FlowboardIcons.Close, contentDescription = "Cerrar")
                        }
                    } else {
                        IconButton(onClick = viewModel::onBack) {
                            Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                state.errorMessage?.let { message ->
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Button(
                    onClick = if (state.step == 1) viewModel::onContinue else viewModel::onSubmit,
                    enabled = !state.isSubmitting,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    if (state.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(if (state.step == 1) "Continuar" else "Registrar colaborador")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StepIndicator(step = state.step)
            if (state.step == 1) {
                PersonalDataStep(
                    state = state,
                    onFirstNameChange = viewModel::onFirstNameChange,
                    onLastNameChange = viewModel::onLastNameChange,
                    onDocumentTypeChange = viewModel::onDocumentTypeChange,
                    onDocumentNumberChange = viewModel::onDocumentNumberChange,
                    onBirthDateChange = viewModel::onBirthDateChange,
                    onEmailChange = viewModel::onEmailChange,
                    onPhoneChange = viewModel::onPhoneChange
                )
            } else {
                ContractAssignmentStep(
                    state = state,
                    onContractTypeChange = viewModel::onContractTypeChange,
                    onHireDateChange = viewModel::onHireDateChange,
                    onContractEndDateChange = viewModel::onContractEndDateChange,
                    onAreaChange = viewModel::onAreaChange,
                    onPositionChange = viewModel::onPositionChange,
                    onDirectManagerChange = viewModel::onDirectManagerChange
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(step: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = if (step == 1) "Paso 1 de 2 · Datos personales" else "Paso 2 de 2 · Contrato y asignación",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            listOf(1, 2).forEach { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index <= step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
