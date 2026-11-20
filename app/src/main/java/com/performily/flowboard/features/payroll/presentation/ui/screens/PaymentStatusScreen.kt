package com.performily.flowboard.features.payroll.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardRefreshBox
import com.performily.flowboard.core.designsystem.component.FlowboardChipRow
import com.performily.flowboard.core.designsystem.component.FlowboardMenuChip
import com.performily.flowboard.core.designsystem.component.SkeletonList
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.presentation.state.PaymentStatusUiState
import com.performily.flowboard.features.payroll.presentation.ui.components.PaymentStatusChip
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollTopAppBar
import com.performily.flowboard.features.payroll.presentation.ui.components.PayslipListItem
import com.performily.flowboard.features.payroll.presentation.ui.components.UpdatePaymentStatusSheet
import com.performily.flowboard.features.payroll.presentation.ui.components.initials
import com.performily.flowboard.features.payroll.presentation.ui.components.label
import com.performily.flowboard.features.payroll.presentation.ui.components.listName
import com.performily.flowboard.features.payroll.presentation.ui.components.paymentReportCountLabel
import com.performily.flowboard.features.payroll.presentation.ui.components.toDisplay
import com.performily.flowboard.features.payroll.presentation.viewmodel.PaymentStatusViewModel

/**
 * Payment status screen for HR (MA-69).
 *
 * The period, area and status filters slide away while the list scrolls down.
 *
 * @param initialPayrollPeriodId payroll period selected when the screen opens, or null for the latest one.
 * @param onBack called when the user navigates back.
 * @param viewModel view model that holds the state of the screen.
 */
@Composable
fun PaymentStatusScreen(
    initialPayrollPeriodId: Long?,
    onBack: () -> Unit,
    viewModel: PaymentStatusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val headerState = rememberCollapsingHeaderState()

    LaunchedEffect(Unit) {
        viewModel.start(initialPayrollPeriodId)
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = { PayrollTopAppBar(title = "Estado de pagos", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        FlowboardRefreshBox(
            isLoading = state.isLoading,
            onRefresh = viewModel::loadReport,
            modifier = Modifier.padding(paddingValues)
        ) {
            CollapsingHeaderLayout(
                state = headerState,
                showHeaderKey = listOf(state.isLoading, state.errorMessage != null, state.selectedPeriod == null, state.entries.size),
                header = {
                    PaymentFilters(
                        state = state,
                        onPeriodSelected = viewModel::onPeriodSelected,
                        onAreaSelected = viewModel::onAreaSelected,
                        onStatusSelected = viewModel::onStatusSelected,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                }
            ) { contentPadding ->
                val stateModifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentPadding.calculateTopPadding())
                when {
                    state.isLoading && state.entries.isEmpty() -> SkeletonList(modifier = stateModifier)

                    state.errorMessage != null -> Box(stateModifier) {
                        MessageState(
                            title = "No se pudo cargar el estado de pagos",
                            message = state.errorMessage.orEmpty(),
                            actionLabel = "Reintentar",
                            onAction = { viewModel.loadPeriods() }
                        )
                    }

                    state.selectedPeriod == null -> Box(stateModifier) {
                        MessageState(
                            title = "No hay períodos de planilla",
                            message = "Todavía no se registró ningún período de planilla.",
                            actionLabel = null,
                            onAction = {}
                        )
                    }

                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = contentPadding.calculateTopPadding() + 4.dp,
                            bottom = Spacing.xl,
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                    ) {
                        item {
                            Text(
                                text = paymentReportCountLabel(state.entries.size, state.selectedStatus),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(state.entries, key = { it.payslip.id }) { entry ->
                            PayslipListItem(
                                name = entry.listName,
                                initials = entry.initials,
                                amount = entry.payslip.netAmount.toDisplay(),
                                status = { PaymentStatusChip(entry.payslip.payment.status) },
                                onClick = { viewModel.onEntryClick(entry) }
                            )
                        }
                    }
                }
            }
        }
    }

    state.sheet?.let { sheet ->
        UpdatePaymentStatusSheet(
            sheet = sheet,
            onChoiceChange = viewModel::onSheetChoiceChange,
            onPaidOnChange = viewModel::onSheetPaidOnChange,
            onReasonChange = viewModel::onSheetReasonChange,
            onSave = viewModel::onSheetSave,
            onDismiss = viewModel::onSheetDismiss
        )
    }
}

/**
 * Filters of MA-69: payroll period, area and payment status.
 *
 * The chips stay on one line that scrolls sideways, so applying a filter never pushes the list
 * down. Area and status are optional filters with an "x" to remove them; the period is required.
 *
 * @param state current screen state.
 * @param onPeriodSelected called with the period the user picks.
 * @param onAreaSelected called with the area the user picks, or null for every area.
 * @param onStatusSelected called with the status the user picks, or null to remove the filter.
 * @param modifier modifier applied to the filter row.
 */
@Composable
private fun PaymentFilters(
    state: PaymentStatusUiState,
    onPeriodSelected: (PayrollPeriod) -> Unit,
    onAreaSelected: (PayrollArea?) -> Unit,
    onStatusSelected: (PaymentStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardChipRow(modifier = modifier) {
        FlowboardMenuChip(
            name = "Período",
            selectedLabel = state.selectedPeriod?.period?.label,
            options = state.periods,
            optionLabel = { it.period.label },
            onSelect = onPeriodSelected,
            icon = FlowboardIcons.Calendar,
            showNameWithValue = false
        )
        FlowboardMenuChip(
            name = "Área",
            selectedLabel = state.selectedArea?.name,
            options = state.areas,
            optionLabel = { it.name },
            onSelect = { onAreaSelected(it) },
            onClear = { onAreaSelected(null) }
        )
        FlowboardMenuChip(
            name = "Estado",
            selectedLabel = state.selectedStatus?.label(),
            options = PaymentStatus.entries,
            optionLabel = { it.label() },
            onSelect = { onStatusSelected(it) },
            onClear = { onStatusSelected(null) }
        )
    }
}
