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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Spacing
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
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar


/**
 * Three-step form to create a request: type, detail and confirmation.
 *
 * The main action of each step sits in a bottom bar that stays above the system navigation bar
 * and the keyboard.
 *
 * @param onClose called when the form is closed from the first step.
 * @param onSubmitted called with the result message once the request is sent.
 * @param viewModel new request view model.
 */
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
            FlowboardTopBar(
                title = "Nueva solicitud",
                onClose = if (state.step == NewRequestStep.TYPE) onClose else null,
                onBack = if (state.step == NewRequestStep.TYPE) null else ({ viewModel.goBack() })
            )
        },
        bottomBar = {
            if (!state.isLoading && state.types.isNotEmpty()) {
                NewRequestActions(
                    state = state,
                    onContinue = viewModel::continueToDetail,
                    onReview = viewModel::continueToConfirmation,
                    onSubmit = viewModel::submit,
                    onEdit = { viewModel.goBack() }
                )
            }
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
            else -> Column(modifier = contentModifier.padding(horizontal = Spacing.screenHorizontal)) {
                StepHeader(
                    step = state.step.number,
                    total = NewRequestStep.entries.size,
                    label = state.step.label,
                    modifier = Modifier.padding(top = Spacing.s, bottom = Spacing.l)
                )
                when (state.step) {
                    NewRequestStep.TYPE -> TypeStep(state = state, onTypeSelected = viewModel::onTypeSelected)
                    NewRequestStep.DETAIL -> DetailStep(state = state, viewModel = viewModel)
                    NewRequestStep.CONFIRMATION -> ConfirmationStep(state = state)
                }
            }
        }
    }
}

/**
 * Bottom bar with the main action of the current step.
 *
 * @param state form state.
 * @param onContinue called when "Continuar" is tapped in the first step.
 * @param onReview called when "Revisar solicitud" is tapped in the detail step.
 * @param onSubmit called when "Enviar solicitud" is tapped in the confirmation step.
 * @param onEdit called when "Editar" is tapped in the confirmation step.
 */
@Composable
private fun NewRequestActions(
    state: NewRequestUiState,
    onContinue: () -> Unit,
    onReview: () -> Unit,
    onSubmit: () -> Unit,
    onEdit: () -> Unit
) {
    if (state.step != NewRequestStep.TYPE && state.selectedType == null) return
    FlowboardBottomActionBar {
        when (state.step) {
            NewRequestStep.TYPE -> Button(
                onClick = onContinue,
                enabled = state.selectedType != null,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Continuar")
            }
            NewRequestStep.DETAIL -> Button(
                onClick = onReview,
                enabled = state.canReview,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Revisar solicitud")
            }
            NewRequestStep.CONFIRMATION -> {
                OutlinedButton(
                    onClick = onEdit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text("Editar")
                }
                Button(
                    onClick = onSubmit,
                    enabled = !state.isSubmitting,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
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
            }
        }
    }
}

/** First step: list of the active request types to choose from. */
@Composable
private fun ColumnScope.TypeStep(
    state: NewRequestUiState,
    onTypeSelected: (RequestType) -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
    ) {
        Text("¿Qué quieres solicitar?", fontSize = 20.sp)
        state.types.forEach { type ->
            val selected = type.id == state.selectedType?.id
            RequestCard(
                onClick = { onTypeSelected(type) },
                containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.White,
                border = BorderStroke(1.dp, Divider)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
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
}

/** Second step: period, dynamic fields, attachments and approver of the request. */
@Composable
private fun ColumnScope.DetailStep(state: NewRequestUiState, viewModel: NewRequestViewModel) {
    val type = state.selectedType ?: return
    val openPicker = rememberAttachmentPicker(onPicked = viewModel::onFilePicked)
    val availability = state.availability
    val period = state.period

    Column(
        modifier = Modifier
            .weight(1f)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.l)
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.l)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
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
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
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
}

/** Card that tells who will receive the request. */
@Composable
private fun ApproverCard(state: NewRequestUiState) {
    RequestCard {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
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

/** Third step: summary of the request before sending it. */
@Composable
private fun ColumnScope.ConfirmationStep(state: NewRequestUiState) {
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
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
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
}
