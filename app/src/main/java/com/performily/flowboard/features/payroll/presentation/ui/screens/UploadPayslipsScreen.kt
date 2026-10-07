package com.performily.flowboard.features.payroll.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.payroll.presentation.state.UploadPayslipsUiState
import com.performily.flowboard.features.payroll.presentation.ui.components.DuplicatePayslipDialog
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollErrorBanner
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollTopAppBar
import com.performily.flowboard.features.payroll.presentation.ui.components.PayslipListItem
import com.performily.flowboard.features.payroll.presentation.ui.components.PayslipUploadZone
import com.performily.flowboard.features.payroll.presentation.ui.components.PeriodSelectField
import com.performily.flowboard.features.payroll.presentation.ui.components.PublicationStatusChip
import com.performily.flowboard.features.payroll.presentation.ui.components.initials
import com.performily.flowboard.features.payroll.presentation.ui.components.listName
import com.performily.flowboard.features.payroll.presentation.ui.components.publishPayslipsLabel
import com.performily.flowboard.features.payroll.presentation.ui.components.toDisplay
import com.performily.flowboard.features.payroll.presentation.ui.components.toMaskedDisplay
import com.performily.flowboard.features.payroll.presentation.ui.components.uploadedPayslipsLabel
import com.performily.flowboard.features.payroll.presentation.viewmodel.UploadPayslipsViewModel

/** MA-67 · Cargar boletas (RR.HH.) y MA-68 · Boleta duplicada. */
@Composable
fun UploadPayslipsScreen(
    onBack: () -> Unit,
    onViewPaymentStatus: (payrollPeriodId: Long?) -> Unit,
    viewModel: UploadPayslipsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = { PayrollTopAppBar(title = "Boletas y pagos", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Button(
                onClick = viewModel::onPublishClick,
                enabled = state.canPublish,
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp)
            ) {
                Text(if (state.isPublishing) "Publicando..." else publishPayslipsLabel(state.underReviewCount))
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                PeriodSelectField(
                    periods = state.periods,
                    selected = state.selectedPeriod,
                    onSelect = viewModel::onPeriodSelected,
                    enabled = !state.isUploading,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = { onViewPaymentStatus(state.selectedPeriod?.id) },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(FlowboardIcons.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = "Ver estado de pagos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            item {
                PayslipUploadZone(
                    enabled = state.selectedPeriod != null && !state.isUploading && !state.isPublishing,
                    supportingText = state.upload
                        ?.let { "Cargando ${it.current} de ${it.total}..." }
                        ?: "Solo PDF · una boleta por colaborador",
                    onFilesSelected = viewModel::onFilesSelected
                )
            }
            state.bannerMessage?.let { message ->
                item { PayrollErrorBanner(message = message) }
            }
            payslipsSection(state = state, onRetry = viewModel::loadPeriods)
        }
    }

    state.duplicate?.let { duplicate ->
        DuplicatePayslipDialog(
            employeeName = duplicate.employeeName,
            periodLabel = duplicate.periodLabel,
            onReplace = viewModel::onReplaceConfirmed,
            onCancel = viewModel::onReplaceCancelled
        )
    }
}

private fun LazyListScope.payslipsSection(
    state: UploadPayslipsUiState,
    onRetry: () -> Unit
) {
    when {
        (state.isLoadingPeriods || state.isLoadingPayslips) && state.entries.isEmpty() -> item {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        state.errorMessage != null -> item {
            MessageState(
                title = "No se pudieron cargar las boletas",
                message = state.errorMessage,
                actionLabel = "Reintentar",
                onAction = onRetry
            )
        }

        state.selectedPeriod == null -> item {
            MessageState(
                title = "No hay períodos de planilla",
                message = "Todavía no se registró ningún período de planilla.",
                actionLabel = null,
                onAction = {}
            )
        }

        else -> {
            item {
                Text(
                    text = uploadedPayslipsLabel(state.entries.size),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(state.entries, key = { it.payslip.id }) { entry ->
                PayslipListItem(
                    name = entry.listName,
                    initials = entry.initials,
                    // Con el diálogo de MA-68 abierto, los montos quedan ocultos detrás del velo.
                    amount = if (state.duplicate != null) {
                        entry.payslip.netAmount.toMaskedDisplay()
                    } else {
                        entry.payslip.netAmount.toDisplay()
                    },
                    status = { PublicationStatusChip(entry.payslip.publicationStatus) }
                )
            }
        }
    }
}

@Composable
internal fun MessageState(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 32.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}
