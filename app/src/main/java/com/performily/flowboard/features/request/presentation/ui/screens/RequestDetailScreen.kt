package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import com.performily.flowboard.features.request.presentation.state.RequestDetailUiState
import com.performily.flowboard.features.request.presentation.ui.components.AttachmentRow
import com.performily.flowboard.features.request.presentation.ui.components.AttachmentZone
import com.performily.flowboard.features.request.presentation.ui.components.CancelRequestDialog
import com.performily.flowboard.features.request.presentation.ui.components.KeyValue
import com.performily.flowboard.features.request.presentation.ui.components.KeyValueCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestColors
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import com.performily.flowboard.features.request.presentation.ui.components.RequestHistoryTimeline
import com.performily.flowboard.features.request.presentation.ui.components.RequestIcons
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.ui.components.RequestSectionHeader
import com.performily.flowboard.features.request.presentation.ui.components.RequestStatusChip
import com.performily.flowboard.features.request.presentation.ui.components.rememberAttachmentPicker
import com.performily.flowboard.features.request.presentation.ui.components.timelineOf
import com.performily.flowboard.features.request.presentation.viewmodel.RequestDetailViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar


/**
 * Detail of one of the viewer's requests, with a bottom bar to resubmit or cancel it when allowed.
 *
 * @param requestId identifier of the request.
 * @param onBack called when the back button is tapped.
 * @param onFinished called with the result message once the request is resubmitted or cancelled.
 * @param viewModel request detail view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    requestId: Long,
    onBack: () -> Unit,
    onFinished: (message: String) -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(requestId) { viewModel.load(requestId) }

    LaunchedEffect(state.resultMessage) {
        state.resultMessage?.let(onFinished)
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Solicitud ${RequestFormatters.code(requestId)}",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.isLoading && state.request != null && (state.canResubmit || state.canCancel)) {
                DetailActions(
                    state = state,
                    onResubmit = viewModel::resubmit,
                    onCancel = viewModel::showCancelDialog
                )
            }
        }
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
            else -> DetailContent(
                state = state,
                request = request,
                viewModel = viewModel,
                modifier = contentModifier
            )
        }
    }

    val request = state.request
    if (state.isCancelDialogVisible && request != null) {
        CancelRequestDialog(
            message = "La solicitud de ${request.requestTypeName.lowercase()} " +
                "${request.period?.let { "del ${RequestFormatters.shortRange(it)} " } ?: ""}quedará como cancelada " +
                "y se registrará en su historial. Esta acción no se puede deshacer.",
            isCancelling = state.isCancelling,
            onConfirm = viewModel::confirmCancel,
            onDismiss = viewModel::dismissCancelDialog
        )
    }
}

/** Scrollable detail of the request: summary, comment, attachments, data and history. */
@Composable
private fun DetailContent(
    state: RequestDetailUiState,
    request: Request,
    viewModel: RequestDetailViewModel,
    modifier: Modifier = Modifier
) {
    val openPicker = rememberAttachmentPicker(onPicked = viewModel::onFilePicked)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal)
            .padding(top = Spacing.s, bottom = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        SummaryCard(request)

        ResolutionComment(request)

        if (state.canResubmit) {
            state.attachments.forEach { file ->
                AttachmentRow(file = file, onRemove = { viewModel.removeAttachment(file) })
            }
            AttachmentZone(
                required = state.type?.requiresAttachment == true,
                isUploading = state.isUploading,
                onClick = openPicker,
                error = state.attachmentError
            )
        } else {
            request.attachments.forEach { file -> AttachmentRow(file = file) }
        }

        val details = buildList {
            request.fieldValues.forEach { value ->
                val field = state.type?.fields?.firstOrNull { it.key == value.key }
                add(KeyValue(field?.label ?: value.key, RequestFormatters.fieldValue(field, value.value)))
            }
            add(KeyValue("Enviada", RequestFormatters.dateTime(request.submittedAt)))
        }
        KeyValueCard(items = details)

        RequestSectionHeader("Historial")
        RequestHistoryTimeline(items = timelineOf(request, state.viewerId))
    }
}

/**
 * Bottom bar with the action the viewer can take on the request: resubmit it or cancel it.
 *
 * @param state screen state.
 * @param onResubmit called when "Reenviar solicitud" is tapped.
 * @param onCancel called when "Cancelar solicitud" is tapped.
 */
@Composable
private fun DetailActions(
    state: RequestDetailUiState,
    onResubmit: () -> Unit,
    onCancel: () -> Unit
) {
    FlowboardBottomActionBar {
        if (state.canResubmit) {
            Button(
                onClick = onResubmit,
                enabled = !state.isResubmitting && !state.isUploading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                if (state.isResubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(RequestIcons.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("Reenviar solicitud", modifier = Modifier.padding(start = Spacing.s))
                }
            }
        } else if (state.canCancel) {
            OutlinedButton(
                onClick = onCancel,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Text("Cancelar solicitud")
            }
        }
    }
}

/** Card with the type, status, period and approver of the request. */
@Composable
private fun SummaryCard(request: Request) {
    RequestCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(request.requestTypeName, fontSize = 20.sp, modifier = Modifier.weight(1f))
            RequestStatusChip(request.status)
        }
        Text(
            text = RequestFormatters.longPeriodWithAmount(request.period),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Aprobador: ${request.approverName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


/** Comment left by the approver when the request was returned or rejected; shows nothing otherwise. */
@Composable
private fun ResolutionComment(request: Request) {
    val entry = when (request.status) {
        RequestStatus.UNDER_REVIEW -> request.reviewComment
        RequestStatus.REJECTED -> request.lastChange
        else -> null
    } ?: return
    val comment = entry.comment ?: return
    val author = entry.actorName?.let(RequestFormatters::shortName) ?: request.approverName
    RequestCard(containerColor = RequestColors.CommentCard, border = null) {
        Text(
            text = if (request.status == RequestStatus.REJECTED) "Motivo del rechazo · $author" else "Comentario de $author",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = RequestColors.OnCommentCard
        )
        Text(text = "\"$comment\"", style = MaterialTheme.typography.bodyMedium, color = RequestColors.OnCommentCard)
    }
}
