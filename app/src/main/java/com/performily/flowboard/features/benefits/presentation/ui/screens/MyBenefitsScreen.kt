package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitCard
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitChip
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitListRow
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.IconCircle
import com.performily.flowboard.features.benefits.presentation.ui.components.SectionHeader
import com.performily.flowboard.features.benefits.presentation.viewmodel.MyBenefitsViewModel

/** En "Vigentes" se muestran también los últimos entregados, como en MA-58. */
private const val RECENT_DELIVERED = 2

/** MA-58 / MA-59 · Mis beneficios: vigentes y entregados. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBenefitsScreen(
    onBack: () -> Unit,
    viewModel: MyBenefitsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis beneficios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = state.selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                listOf("Vigentes (${state.currentCount})", "Entregados (${state.deliveredCount})")
                    .forEachIndexed { index, title ->
                        Tab(
                            selected = state.selectedTab == index,
                            onClick = { viewModel.onTabSelected(index) },
                            text = { Text(title) }
                        )
                    }
            }
            val benefits = state.benefits
            when {
                state.isLoading -> BenefitsLoading()
                benefits == null -> BenefitsMessageState(
                    title = "No se pudieron cargar tus beneficios",
                    message = state.errorMessage ?: "Inténtalo nuevamente.",
                    actionLabel = "Reintentar",
                    onAction = viewModel::load
                )
                state.selectedTab == 0 -> CurrentTab(benefits)
                else -> DeliveredTab(benefits.delivered)
            }
        }
    }
}

@Composable
private fun CurrentTab(benefits: EmployeeBenefits) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (benefits.current.isEmpty()) {
            item {
                BenefitsMessageState(
                    title = "No tienes beneficios vigentes",
                    message = "Cuando RR.HH. te asigne un beneficio aparecerá aquí."
                )
            }
        }
        items(benefits.current, key = { it.id }) { assignment -> CurrentBenefitCard(assignment) }

        val recent = benefits.delivered.take(RECENT_DELIVERED)
        if (recent.isNotEmpty()) {
            item { SectionHeader("Entregados recientemente") }
            items(recent, key = { "recent-${it.id}" }) { assignment -> DeliveredRow(assignment) }
        }
    }
}

@Composable
private fun DeliveredTab(delivered: List<BenefitAssignment>) {
    if (delivered.isEmpty()) {
        BenefitsMessageState(
            title = "Aún no tienes beneficios entregados",
            message = "Cuando RR.HH. registre la entrega de un beneficio, lo verás aquí."
        )
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
    ) {
        items(delivered, key = { it.id }) { assignment -> DeliveredRow(assignment) }
    }
}

/** Tarjeta de un beneficio vigente (MA-58). */
@Composable
private fun CurrentBenefitCard(assignment: BenefitAssignment) {
    BenefitCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            IconCircle(icon = FlowboardIcons.Gift)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(assignment.benefitTypeName, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Periodo: ${BenefitsFormatters.period(assignment.startDate, assignment.endDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = BenefitsFormatters.quantity(assignment.quantity, assignment.unit),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            BenefitChip(AssignmentStatus.ASSIGNED.label, BenefitsColors.Assigned)
        }
    }
}

/** Fila de un beneficio entregado (MA-59): "Entregado el 15/07/2026 · S/ 1,240.00". */
@Composable
private fun DeliveredRow(assignment: BenefitAssignment) {
    val deliveredOn = assignment.delivery?.deliveredOn
    val detail = listOfNotNull(
        deliveredOn?.let { "Entregado el ${BenefitsFormatters.date(it)}" },
        BenefitsFormatters.quantity(assignment.quantity, assignment.unit)
    ).joinToString(" · ")
    BenefitListRow(
        title = assignment.benefitTypeName,
        subtitle = detail,
        modifier = Modifier.fillMaxWidth(),
        leading = { IconCircle(icon = FlowboardIcons.Gift) },
        trailing = { BenefitChip(AssignmentStatus.DELIVERED.label, BenefitsColors.Delivered) }
    )
}
