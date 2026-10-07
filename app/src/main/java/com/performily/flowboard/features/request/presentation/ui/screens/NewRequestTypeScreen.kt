package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.request.domain.entity.NewRequestField
import com.performily.flowboard.features.request.domain.valueobject.BalanceDeduction
import com.performily.flowboard.features.request.presentation.ui.components.AddFieldSheet
import com.performily.flowboard.features.request.presentation.ui.components.RequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import com.performily.flowboard.features.request.presentation.ui.components.RequestIcons
import com.performily.flowboard.features.request.presentation.ui.components.RequestSectionHeader
import com.performily.flowboard.features.request.presentation.ui.components.RequestSegmented
import com.performily.flowboard.features.request.presentation.viewmodel.NewRequestTypeViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestTypeScreen(
    onClose: () -> Unit,
    onCreated: (message: String) -> Unit,
    viewModel: NewRequestTypeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.resultMessage) {
        state.resultMessage?.let(onCreated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo tipo de solicitud") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(FlowboardIcons.Close, contentDescription = "Cerrar") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nombre") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = state.description,
                    onValueChange = viewModel::onDescriptionChange,
                    label = { Text("Descripción") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Requiere documento adjunto", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "El colaborador no podrá enviar sin sustento",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = state.requiresAttachment, onCheckedChange = viewModel::onRequiresAttachmentChange)
                }
                Text(
                    text = "Descuenta saldo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RequestSegmented(
                    options = BalanceDeduction.entries,
                    selected = state.balanceDeduction,
                    optionLabel = { it.label },
                    onSelect = viewModel::onBalanceDeductionChange
                )
                RequestSectionHeader("Campos del formulario") {
                    TextButton(onClick = viewModel::showFieldSheet) { Text("+ Agregar campo") }
                }
                FieldsCard(fields = state.fields, onRemove = viewModel::removeField)
                state.errorMessage?.let { error ->
                    Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                }
            }
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .height(44.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar tipo")
                }
            }
        }
    }

    state.fieldForm?.let { form ->
        AddFieldSheet(
            label = form.label,
            dataType = form.dataType,
            required = form.required,
            error = form.error,
            onLabelChange = viewModel::onFieldLabelChange,
            onDataTypeChange = viewModel::onFieldDataTypeChange,
            onRequiredChange = viewModel::onFieldRequiredChange,
            onAdd = viewModel::addField,
            onDismiss = viewModel::dismissFieldSheet
        )
    }
}


@Composable
private fun FieldsCard(fields: List<NewRequestField>, onRemove: (NewRequestField) -> Unit) {
    if (fields.isEmpty()) {
        Text(
            text = "Sin campos. El colaborador solo elegirá las fechas y, si corresponde, adjuntará el sustento.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    RequestCard(spacing = 0.dp) {
        fields.forEachIndexed { index, field ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(field.label, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = RequestFormatters.fieldDetail(field.dataType, field.required),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { onRemove(field) }) {
                    Icon(RequestIcons.Delete, contentDescription = "Quitar ${field.label}")
                }
            }
            if (index < fields.lastIndex) HorizontalDivider(color = Divider)
        }
    }
}
