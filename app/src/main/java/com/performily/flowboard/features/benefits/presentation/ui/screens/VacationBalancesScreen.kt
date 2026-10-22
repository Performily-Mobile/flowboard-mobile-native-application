package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardSearchField
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitListRow
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsBanner
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.InitialsAvatar
import com.performily.flowboard.features.benefits.presentation.viewmodel.VacationBalancesViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * Vacation balances screen.
 *
 * Lists the balances of active employees; tapping one opens its detail (MA-63). The search box
 * slides away while the list scrolls down.
 *
 * @param onBack navigates back.
 * @param onEmployeeClick opens the balance detail of the tapped employee.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacationBalancesScreen(
    onBack: () -> Unit,
    onEmployeeClick: (employeeId: Long, employeeName: String) -> Unit,
    viewModel: VacationBalancesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Saldos de vacaciones",
                onBack = onBack
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        val errorMessage = state.errorMessage
        when {
            state.isLoading -> BenefitsLoading(modifier = contentModifier)
            state.errorMessage != null && state.balances.isEmpty() -> BenefitsMessageState(
                title = "No se pudieron cargar los saldos",
                message = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::load,
                modifier = contentModifier
            )
            else -> CollapsingHeaderLayout(
                modifier = Modifier.padding(paddingValues),
                showHeaderKey = listOf(state.filtered.size, errorMessage != null),
                header = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.screenHorizontal)
                            .padding(top = 4.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(Spacing.m)
                    ) {
                        FlowboardSearchField(
                            query = state.query,
                            onQueryChange = viewModel::onQueryChange,
                            placeholder = "Buscar por nombre o área"
                        )
                        if (errorMessage != null && state.balances.isNotEmpty()) {
                            BenefitsBanner(
                                message = errorMessage,
                                icon = FlowboardIcons.Warning,
                                containerColor = BenefitsColors.WarningBanner,
                                contentColor = BenefitsColors.OnWarningBanner,
                                actionLabel = "Reintentar",
                                onAction = viewModel::load
                            )
                        }
                    }
                }
            ) { contentPadding ->
                if (state.filtered.isEmpty()) {
                    BenefitsMessageState(
                        title = "Sin resultados",
                        message = if (state.balances.isEmpty()) "Aún no hay saldos de vacaciones." else "Prueba con otro nombre.",
                        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = contentPadding.calculateTopPadding() + 8.dp,
                            bottom = Spacing.xl,
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal
                        )
                    ) {
                        items(state.filtered, key = { it.employeeId }) { balance ->
                            val name = balance.employeeName ?: "Colaborador #${balance.employeeId}"
                            BenefitListRow(
                                title = name,
                                subtitle = listOfNotNull(
                                    balance.areaName,
                                    "Usados ${BenefitsFormatters.number(balance.usedDays)} de ${BenefitsFormatters.number(balance.accruedDays)}"
                                ).joinToString(" · "),
                                onClick = { onEmployeeClick(balance.employeeId, name) },
                                leading = { InitialsAvatar(name = name) },
                                trailing = {
                                    Text(
                                        text = "${BenefitsFormatters.number(balance.availableDays)} d",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
