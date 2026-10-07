package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.presentation.state.NewRequestStep
import com.performily.flowboard.features.request.presentation.state.NewRequestUiState
import com.performily.flowboard.features.request.presentation.ui.components.AttachmentRow
import com.performily.flowboard.features.request.presentation.ui.components.AttachmentZone
import com.performily.flowboard.features.request.presentation.ui.components.DynamicFieldInput
import com.performily.flowboard.features.request.presentation.ui.components.KeyValue
import com.performily.flowboard.features.request.presentation.ui.components.KeyValueCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestAvatar
import com.performily.flowboard.features.request.presentation.ui.components.RequestBanner
import com.performily.flowboard.features.request.presentation.ui.components.RequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestColors
import com.performily.flowboard.features.request.presentation.ui.components.RequestDateField
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.ui.components.RequestTimeField
import com.performily.flowboard.features.request.presentation.ui.components.StepHeader
import com.performily.flowboard.features.request.presentation.ui.components.rememberAttachmentPicker
import com.performily.flowboard.features.request.presentation.viewmodel.NewRequestViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRequestScreen(
    onClose: () -> Unit,
    onSubmitted: (message: String) -> Unit,
    viewModel: NewRequestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(state.resultMessage) {
        state.resultMessage?.let(onSubmitted)
    }

    BackHandler(enabled = state.step != NewRequestStep.TYPE) { viewModel.goBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva solicitud") },
                navigationIcon = {
                    if (state.step == NewRequestStep.TYPE) {
                        IconButton(onClick = onClose) { Icon(FlowboardIcons.Close, contentDescription = "Cerrar") }
                    } else {
                        IconButton(onClick = { viewModel.goBack() }) {
                            Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> RequestLoading(modifier = contentModifier)
            state.types.isEmpty() -> RequestMessageState(
                title = if (state.loadError != null) "No se pudo cargar el formulario" else "No hay tipos de solicitud activos",
                message = state.loadError ?: "Recursos Humanos aún no ha habilitado tipos de solicitud.",
                actionLabel = if (state.loadError != null) "Reintentar" else null,
                onAction = viewModel::load,
                modifier = contentModifier
            )
            else -> Column(modifier = contentModifier.padding(horizontal = 16.dp)) {
                StepHeader(
                    step = state.step.number,
                    total = NewRequestStep.entries.size,
                    label = state.step.label,
                    modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                )
                when (state.step) {
                    NewRequestStep.TYPE -> TypeStep(
                        state = state,
                        onTypeSelected = viewModel::onTypeSelected,
                        onContinue = viewModel::continueToDetail
                    )
                    NewRequestStep.DETAIL -> DetailStep(state = state, viewModel = viewModel)
                    NewRequestStep.CONFIRMATION -> ConfirmationStep(
                        state = state,
                        onSubmit = viewModel::submit,
                        onEdit = { viewModel.goBack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.TypeStep(
    state: NewRequestUiState,
    onTypeSelected: (RequestType) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("¿Qué quieres solicitar?", fontSize = 20.sp)
        state.types.forEach { type ->
            val selected = type.id == state.selectedType?.id
            RequestCard(
                onClick = { onTypeSelected(type) },
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.White,
                border = BorderStroke(1.dp, Divider)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected, onClick = { onTypeSelected(type) }, modifier = Modifier.size(24.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(type.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = RequestFormatters.typeHint(type, state.availability?.availableDays),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
    PrimaryButton(text = "Continuar", enabled = state.selectedType != null, onClick = onContinue)
}

@Composable
private fun ColumnScope.DetailStep(state: NewRequestUiState, viewModel: NewRequestViewModel) {
    val type = state.selectedType ?: return
    val openPicker = rememberAttachmentPicker(onPicked = viewModel::onFilePicked)
    val availability = state.availability
    val period = state.period

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (state.exceedsBalance && availability != null) {
            RequestBanner(
                message = "Solicitaste ${RequestFormatters.days(state.requestedDays)} y tu saldo disponible es de " +
                    "${RequestFormatters.number(availability.availableDays)}. Ajusta el periodo o consulta con Recursos Humanos.",
                icon = FlowboardIcons.Warning,
                containerColor = RequestColors.ErrorBanner,
                contentColor = RequestColors.OnErrorBanner
            )
        }

        if (state.canUseHours) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Por horas", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "Una fracción de la jornada de un solo día",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = state.byHours, onCheckedChange = viewModel::onByHoursChange)
            }
        }

        if (state.byHours && state.canUseHours) {
            RequestDateField(
                label = "Fecha",
                value = state.startDate,
                onValueChange = viewModel::onStartDateChange,
                isError = state.periodError != null && state.startDate == null,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RequestTimeField(
                    label = "Hora de inicio",
                    value = state.startTime,
                    onValueChange = viewModel::onStartTimeChange,
                    modifier = Modifier.weight(1f)
                )
                RequestTimeField(
                    label = "Hora de fin",
                    value = state.endTime,
                    onValueChange = viewModel::onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RequestDateField(
                    label = "Fecha de inicio",
                    value = state.startDate,
                    onValueChange = viewModel::onStartDateChange,
                    isError = state.periodError != null && state.startDate == null,
                    modifier = Modifier.weight(1f)
                )
                RequestDateField(
                    label = "Fecha de fin",
                    value = state.endDate,
                    onValueChange = viewModel::onEndDateChange,
                    minDate = state.startDate,
                    isError = state.exceedsBalance,
                    supportingText = if (state.exceedsBalance) "Supera tu saldo" else null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        state.periodError?.let { error ->
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        if (period != null && type.deductsVacationDays && availability != null && !state.exceedsBalance) {
            RequestBanner(
                message = "${RequestFormatters.days(period.days)}. Saldo disponible: ${RequestFormatters.days(availability.availableDays)}.",
                icon = FlowboardIcons.Check,
                containerColor = RequestColors.OkBanner,
                contentColor = RequestColors.OnOkBanner
            )
        }

        type.fields.forEach { field ->
            DynamicFieldInput(
                field = field,
                value = state.values[field.key].orEmpty(),
                onValueChange = { viewModel.onFieldChange(field.key, it) },
                error = state.fieldErrors[field.key]
            )
        }

        AttachmentZone(
            required = type.requiresAttachment,
            isUploading = state.isUploading,
            onClick = openPicker,
            error = state.attachmentError
        )
        state.attachments.forEach { file ->
            AttachmentRow(file = file, onRemove = { viewModel.removeAttachment(file) })
        }

        ApproverCard(state = state)
    }
    PrimaryButton(text = "Revisar solicitud", enabled = state.canReview, onClick = viewModel::continueToConfirmation)
}

@Composable
private fun ApproverCard(state: NewRequestUiState) {
    RequestCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RequestAvatar(name = state.approverName)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Se enviará a ${RequestFormatters.shortName(state.approverName)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = if (state.approver != null) {
                        "Tu jefe directo según la jerarquía registrada"
                    } else {
                        "No tienes jefe directo asignado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ColumnScope.ConfirmationStep(
    state: NewRequestUiState,
    onSubmit: () -> Unit,
    onEdit: () -> Unit
) {
    val type = state.selectedType ?: return
    val period = state.period
    val availability = state.availability
    val items = buildList {
        add(KeyValue("Tipo", type.name))
        add(KeyValue("Periodo", RequestFormatters.longPeriod(period)))
        add(KeyValue(if (period?.hasHours == true) "Horas" else "Días", RequestFormatters.amount(period)))
        if (type.deductsVacationDays && availability != null) {
            add(KeyValue("Saldo después de aprobar", RequestFormatters.days(availability.afterUsing(state.requestedDays))))
        }
        type.fields.forEach { field ->
            state.values[field.key]?.takeIf { it.isNotBlank() }?.let { value ->
                add(KeyValue(field.label, RequestFormatters.fieldValue(field, value)))
            }
        }
        if (state.attachments.isNotEmpty()) {
            add(KeyValue("Adjuntos", state.attachments.joinToString("\n") { it.fileName }))
        }
        add(
            KeyValue(
                "Aprobador",
                if (state.approver != null) "${state.approverName} · Jefe directo" else "Recursos Humanos"
            )
        )
    }

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Revisa tu solicitud", fontSize = 20.sp)
        KeyValueCard(items = items)
        Text(
            text = "${RequestFormatters.shortName(state.approverName)} recibirá una notificación en su teléfono. " +
                "Podrás seguir el estado en Solicitudes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.errorMessage?.let { error ->
            Text(error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
    Column(
        modifier = Modifier.padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Enviar solicitud")
            }
        }
        OutlinedButton(
            onClick = onEdit,
            enabled = !state.isSubmitting,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text("Editar")
        }
    }
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .height(44.dp)
    ) {
        Text(text)
    }
}
