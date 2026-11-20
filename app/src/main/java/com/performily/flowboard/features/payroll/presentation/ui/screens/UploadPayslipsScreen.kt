package com.performily.flowboard.features.payroll.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.component.FlowboardChipRow
import com.performily.flowboard.core.designsystem.component.FlowboardEmptyState
import com.performily.flowboard.core.designsystem.component.FlowboardLeadingIcon
import com.performily.flowboard.core.designsystem.component.FlowboardListRow
import com.performily.flowboard.core.designsystem.component.FlowboardMenuChip
import com.performily.flowboard.core.designsystem.component.SkeletonList
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.presentation.state.UploadPayslipsUiState
import com.performily.flowboard.features.payroll.presentation.ui.components.DuplicatePayslipDialog
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollErrorBanner
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollTopAppBar
import com.performily.flowboard.features.payroll.presentation.ui.components.PayslipListItem
import com.performily.flowboard.features.payroll.presentation.ui.components.PayslipUploadZone
import com.performily.flowboard.features.payroll.presentation.ui.components.PublicationStatusChip
import com.performily.flowboard.features.payroll.presentation.ui.components.initials
import com.performily.flowboard.features.payroll.presentation.ui.components.listName
import com.performily.flowboard.features.payroll.presentation.ui.components.publishPayslipsLabel
import com.performily.flowboard.features.payroll.presentation.ui.components.toDisplay
import com.performily.flowboard.features.payroll.presentation.ui.components.toMaskedDisplay
import com.performily.flowboard.features.payroll.presentation.ui.components.uploadedPayslipsLabel
import com.performily.flowboard.features.payroll.presentation.viewmodel.UploadPayslipsViewModel

/**
 * Payslip upload screen for HR (MA-67), including the duplicate payslip prompt (MA-68).
 *
 * @param onBack called when the user navigates back
 * @param onViewPaymentStatus called with the selected payroll period id to open the payment status screen
 * @param viewModel view model that holds the state of the screen
 */
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
            FlowboardBottomActionBar {
                Button(
                    onClick = viewModel::onPublishClick,
                    enabled = state.canPublish,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        when {
                            state.isPublishing -> "Publicando..."
                            state.underReviewCount == 0 -> "Sin boletas por publicar"
                            else -> publishPayslipsLabel(state.underReviewCount)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.s,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            item {
                FlowboardChipRow(contentPadding = PaddingValues(0.dp)) {
                    FlowboardMenuChip(
                        name = "Período",
                        selectedLabel = state.selectedPeriod?.period?.label,
                        options = state.periods,
                        optionLabel = { it.period.label },
                        onSelect = viewModel::onPeriodSelected,
                        icon = FlowboardIcons.Calendar,
                        showNameWithValue = false,
                        enabled = !state.isUploading && state.periods.isNotEmpty()
                    )
                }
            }
            item {
                PayslipUploadZone(
                    enabled = state.selectedPeriod != null && !state.isUploading && !state.isPublishing &&
                        !state.isLoadingPeriods && !state.isLoadingPayslips && state.errorMessage == null,
                    supportingText = state.upload
                        ?.let { "Cargando ${it.current} de ${it.total}..." }
                        ?: "Solo PDF, una boleta por colaborador",
                    onFilesSelected = viewModel::onFilesSelected
                )
            }
            item {
                FlowboardListRow(
                    headline = "Estado de pagos",
                    supporting = "Pagadas, pendientes y observadas",
                    leading = { FlowboardLeadingIcon(icon = FlowboardIcons.CreditCard) },
                    trailing = {
                        Icon(
                            imageVector = FlowboardIcons.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    onClick = { onViewPaymentStatus(state.selectedPeriod?.id) }
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

/**
 * Adds the payslip list, or its loading, error and empty states, to the screen list.
 *
 * While the duplicate dialog (MA-68) is open, the amounts are masked behind the scrim.
 *
 * @param state current screen state
 * @param onRetry called when the user taps the retry button
 */
private fun LazyListScope.payslipsSection(
    state: UploadPayslipsUiState,
    onRetry: () -> Unit
) {
    when {
        (state.isLoadingPeriods || state.isLoadingPayslips) && state.entries.isEmpty() -> item {
            SkeletonList(itemCount = 4, contentPadding = PaddingValues(0.dp))
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
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = Spacing.m)
                )
            }
            items(state.entries, key = { it.payslip.id }) { entry ->
                PayslipListItem(
                    name = entry.listName,
                    initials = entry.initials,
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

/**
 * Centered message for the error and empty states of the payroll lists.
 *
 * @param title main line.
 * @param message explanation below the title.
 * @param actionLabel text of the action button, or null to hide it.
 * @param onAction called when the action button is tapped.
 */
@Composable
internal fun MessageState(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    FlowboardEmptyState(
        title = title,
        message = message,
        icon = if (actionLabel == "Reintentar") FlowboardIcons.Warning else FlowboardIcons.CreditCard,
        modifier = Modifier,
        action = if (actionLabel != null) {
            { OutlinedButton(onClick = onAction) { Text(actionLabel) } }
        } else {
            null
        }
    )
}
