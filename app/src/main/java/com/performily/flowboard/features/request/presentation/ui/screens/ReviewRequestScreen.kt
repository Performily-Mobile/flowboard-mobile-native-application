package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.presentation.state.ReviewRequestUiState
import com.performily.flowboard.features.request.presentation.ui.components.AttachmentRow
import com.performily.flowboard.features.request.presentation.ui.components.KeyValue
import com.performily.flowboard.features.request.presentation.ui.components.KeyValueCard
import com.performily.flowboard.features.request.presentation.ui.components.RejectRequestDialog
import com.performily.flowboard.features.request.presentation.ui.components.RequestAvatar
import com.performily.flowboard.features.request.presentation.ui.components.RequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import com.performily.flowboard.features.request.presentation.ui.components.RequestHistoryTimeline
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.ui.components.RequestSectionHeader
import com.performily.flowboard.features.request.presentation.ui.components.RequestStatusChip
import com.performily.flowboard.features.request.presentation.ui.components.ReturnForReviewSheet
import com.performily.flowboard.features.request.presentation.ui.components.timelineOf
import com.performily.flowboard.features.request.presentation.viewmodel.ReviewRequestViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRequestScreen(
    requestId: Long,
    onBack: () -> Unit,
    onResolved: (message: String) -> Unit,
    viewModel: ReviewRequestViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestId) { viewModel.load(requestId) }

    LaunchedEffect(state.resultMessage) {
        state.resultMessage?.let(onResolved)
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (state.request == null || state.canResolve) "Revisar solicitud" else "Solicitud ${RequestFormatters.code(requestId)}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        val request = state.request
        when {
            state.isLoading -> RequestLoading(modifier = contentModifier)
            request == null -> RequestMessageState(
                title = "No se pudo cargar la solicitud",
                message = state.errorMessage ?: "Inténtalo nuevamente.",
                actionLabel = "Reintentar",
                onAction = viewModel::retry,
                modifier = contentModifier
            )
            else -> ReviewContent(
                state = state,
                request = request,
                onReturn = viewModel::showReturnSheet,
                onReject = viewModel::showRejectDialog,
                onApprove = viewModel::approve,
                modifier = contentModifier
            )
        }
    }

    val request = state.request ?: return
    if (state.isRejectDialogVisible) {
        RejectRequestDialog(
            requesterName = RequestFormatters.shortName(request.requesterName),
            reason = state.rejectReason,
            error = state.rejectError,
            isSaving = state.isProcessing,
            onReasonChange = viewModel::onRejectReasonChange,
            onConfirm = viewModel::confirmReject,
            onDismiss = viewModel::dismissRejectDialog
        )
    }
    if (state.isReturnSheetVisible) {
        ReturnForReviewSheet(
            requesterFirstName = request.requesterName.substringBefore(" "),
            comment = state.returnComment,
            error = state.returnError,
            isSaving = state.isProcessing,
            onCommentChange = viewModel::onReturnCommentChange,
            onConfirm = viewModel::confirmReturn,
            onDismiss = viewModel::dismissReturnSheet
        )
    }
}

@Composable
private fun ReviewContent(
    state: ReviewRequestUiState,
    request: Request,
    onReturn: () -> Unit,
    onReject: () -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RequesterCard(request)
            KeyValueCard(items = detailItems(state, request))
            request.attachments.forEach { file -> AttachmentRow(file = file) }
            Text(
                text = "Enviada el ${RequestFormatters.dateTime(request.submittedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!state.canResolve) {
                RequestSectionHeader("Historial")
                RequestHistoryTimeline(items = timelineOf(request, state.viewerId))
            }
        }

        if (state.canResolve) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onReturn, enabled = !state.isProcessing) { Text("Devolver") }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onReject, enabled = !state.isProcessing, shape = RoundedCornerShape(8.dp)) {
                        Text("Rechazar")
                    }
                    Button(onClick = onApprove, enabled = !state.isProcessing, shape = RoundedCornerShape(8.dp)) {
                        if (state.isProcessing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Aprobar")
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun RequesterCard(request: Request) {
    RequestCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RequestAvatar(name = request.requesterName, size = 48.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = RequestFormatters.shortName(request.requesterName),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                request.requester?.jobDescription?.let { job ->
                    Text(job, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            RequestStatusChip(request.status)
        }
    }
}


private fun detailItems(state: ReviewRequestUiState, request: Request): List<KeyValue> = buildList {
    add(KeyValue("Tipo", request.requestTypeName))
    add(KeyValue("Periodo", RequestFormatters.longPeriod(request.period)))
    add(KeyValue(if (request.period?.hasHours == true) "Horas" else "Días", RequestFormatters.amount(request.period)))
    state.availability?.let { availability ->
        add(
            KeyValue(
                "Saldo del colaborador",
                "${RequestFormatters.days(availability.availableDays)} → " +
                    "${RequestFormatters.number(availability.afterUsing(request.requestedDays))} tras aprobar"
            )
        )
    }
    request.fieldValues.forEach { value ->
        val field = state.type?.fields?.firstOrNull { it.key == value.key }
        add(KeyValue(field?.label ?: value.key, RequestFormatters.fieldValue(field, value.value)))
    }
    add(KeyValue("Aprobador", request.approverName))
}
