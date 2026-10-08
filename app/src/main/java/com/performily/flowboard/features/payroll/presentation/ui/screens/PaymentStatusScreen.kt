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

/** MA-69 · Estado de pagos (RR.HH.). */
@Composable
fun PaymentStatusScreen(
    initialPayrollPeriodId: Long?,
    onBack: () -> Unit,
    viewModel: PaymentStatusViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                PaymentFilters(
                    state = state,
                    onPeriodSelected = viewModel::onPeriodSelected,
                    onAreaSelected = viewModel::onAreaSelected,
                    onStatusSelected = viewModel::onStatusSelected
                )
            }

            when {
                state.isLoading && state.entries.isEmpty() -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> item {
                    MessageState(
                        title = "No se pudo cargar el estado de pagos",
                        message = state.errorMessage.orEmpty(),
                        actionLabel = "Reintentar",
                        onAction = { viewModel.loadPeriods() }
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
                            text = paymentReportCountLabel(state.entries.size, state.selectedStatus),
                            style = MaterialTheme.typography.bodySmall,
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

/** Filtros de MA-69: período, área y estado de pago. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentFilters(
    state: PaymentStatusUiState,
    onPeriodSelected: (PayrollPeriod) -> Unit,
    onAreaSelected: (PayrollArea?) -> Unit,
    onStatusSelected: (PaymentStatus?) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
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
            // "Todas las áreas" quita el filtro sin perder la flecha del diseño.
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
