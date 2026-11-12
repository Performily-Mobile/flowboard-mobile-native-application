package com.performily.flowboard.features.payroll.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.presentation.ui.components.MyPayslipCard
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollErrorBanner
import com.performily.flowboard.features.payroll.presentation.ui.components.PayrollMessageState
import com.performily.flowboard.features.payroll.presentation.viewmodel.MyPayslipsViewModel
import com.performily.flowboard.core.designsystem.component.SkeletonList

/**
 * "Boletas" tab of the employee (MA-65): the published payslips of a year, the most recent first.
 *
 * The year selector lives in a header that slides away while the list scrolls.
 *
 * @param onPayslipClick called with the identifier of the tapped payslip.
 * @param viewModel my payslips view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPayslipsScreen(
    onPayslipClick: (payslipId: Long) -> Unit,
    viewModel: MyPayslipsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val headerState = rememberCollapsingHeaderState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = { FlowboardTopBar(title = "Boletas") }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CollapsingHeaderLayout(
                state = headerState,
                showHeaderKey = listOf(
                    state.selectedYear,
                    state.payslips.size,
                    state.isLoading,
                    state.errorMessage != null
                ),
                header = {
                    FlowboardTabs(
                        options = state.years,
                        selected = state.selectedYear,
                        onSelect = viewModel::selectYear,
                        label = { it.toString() },
                        modifier = Modifier.padding(
                            horizontal = Spacing.screenHorizontal,
                            vertical = Spacing.s
                        )
                    )
                }
            ) { contentPadding ->
                val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                when {
                    state.isLoading -> SkeletonList(modifier = stateModifier.fillMaxSize())
                    state.errorMessage != null && state.payslips.isEmpty() -> PayrollMessageState(
                        title = "No se pudieron cargar tus boletas",
                        message = state.errorMessage.orEmpty(),
                        modifier = stateModifier,
                        actionLabel = "Reintentar",
                        onAction = viewModel::refresh
                    )
                    state.payslips.isEmpty() -> PayrollMessageState(
                        title = "Sin boletas en ${state.selectedYear}",
                        message = "Cuando RR. HH. publique una boleta, la verás aquí.",
                        modifier = stateModifier
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal,
                            top = contentPadding.calculateTopPadding() + Spacing.xs,
                            bottom = Spacing.xl
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                    ) {
                        state.errorMessage?.let { message ->
                            item(key = "error") { PayrollErrorBanner(message = message) }
                        }
                        items(state.payslips, key = { it.id }) { payslip ->
                            MyPayslipCard(payslip = payslip, onClick = { onPayslipClick(payslip.id) })
                        }
                    }
                }
            }
        }
    }
}
