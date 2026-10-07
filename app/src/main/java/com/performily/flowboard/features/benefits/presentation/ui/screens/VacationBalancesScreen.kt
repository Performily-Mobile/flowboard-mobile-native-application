package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitListRow
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsBanner
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.InitialsAvatar
import com.performily.flowboard.features.benefits.presentation.viewmodel.VacationBalancesViewModel

/**
 * Vacation balances screen.
 *
 * Lists the balances of active employees; tapping one opens its detail (MA-63).
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
            TopAppBar(
                title = { Text("Saldos de vacaciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
            ) {
                if (errorMessage != null && state.balances.isNotEmpty()) {
                    item {
                        BenefitsBanner(
                            message = errorMessage,
                            icon = FlowboardIcons.Warning,
                            containerColor = BenefitsColors.WarningBanner,
                            contentColor = BenefitsColors.OnWarningBanner,
                            modifier = Modifier.padding(bottom = 8.dp),
                            actionLabel = "Reintentar",
                            onAction = viewModel::load
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChange,
                        placeholder = { Text("Buscar por nombre o área") },
                        leadingIcon = { Icon(FlowboardIcons.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
                if (state.filtered.isEmpty()) {
                    item {
                        BenefitsMessageState(
                            title = "Sin resultados",
                            message = if (state.balances.isEmpty()) "Aún no hay saldos de vacaciones." else "Prueba con otro nombre."
                        )
                    }
                }
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
