package com.performily.flowboard.features.payroll.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.domain.entity.PayrollArea
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod
import com.performily.flowboard.features.payroll.domain.valueobject.PaymentStatus
import com.performily.flowboard.features.payroll.presentation.state.PaymentStatusUiState
import com.performily.flowboard.features.payroll.presentation.ui.components.PaymentStatusChip
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollFilterChip
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
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.isLoading, state.errorMessage != null, state.selectedPeriod == null, state.entries.size),
            header = {
                PaymentFilters(
                    state = state,
                    onPeriodSelected = viewModel::onPeriodSelected,
                    onAreaSelected = viewModel::onAreaSelected,
                    onStatusSelected = viewModel::onStatusSelected,
                    modifier = Modifier
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp)
                )
            }
        ) { contentPadding ->
            val stateModifier = Modifier
                .fillMaxSize()
                .padding(top = contentPadding.calculateTopPadding())
            when {
                state.isLoading && state.entries.isEmpty() -> Box(
                    modifier = stateModifier.padding(top = Spacing.xxl),
                    contentAlignment = Alignment.TopCenter
                ) {
                    CircularProgressIndicator()
                }

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
 * The area menu starts with "Todas las áreas", which removes the area filter while keeping the
 * arrow of the design.
 *
 * @param state current screen state.
 * @param onPeriodSelected called with the period the user picks.
 * @param onAreaSelected called with the area the user picks, or null for every area.
 * @param onStatusSelected called with the status the user picks, or null to remove the filter.
 * @param modifier modifier applied to the filter row.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentFilters(
    state: PaymentStatusUiState,
    onPeriodSelected: (PayrollPeriod) -> Unit,
    onAreaSelected: (PayrollArea?) -> Unit,
    onStatusSelected: (PaymentStatus?) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = modifier.fillMaxWidth()
    ) {
        PayrollFilterChip(
            label = state.selectedPeriod?.period?.label ?: "Período",
            selected = state.selectedPeriod != null,
            options = state.periods,
            optionLabel = { it.period.label },
            onSelect = onPeriodSelected
        )
        PayrollFilterChip(
            label = state.selectedArea?.let { "Área: ${it.name}" } ?: "Área",
            selected = state.selectedArea != null,
            options = listOf<PayrollArea?>(null) + state.areas,
            optionLabel = { it?.name ?: "Todas las áreas" },
            onSelect = onAreaSelected
        )
        PayrollFilterChip(
            label = state.selectedStatus?.label() ?: "Estado",
            selected = state.selectedStatus != null,
            options = PaymentStatus.entries,
            optionLabel = { it.label() },
            onSelect = { onStatusSelected(it) },
            clearable = true,
            onClear = { onStatusSelected(null) }
        )
    }
}
