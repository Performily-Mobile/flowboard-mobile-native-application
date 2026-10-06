package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.presentation.state.OrganizationChartMode
import com.performily.flowboard.features.workspace.presentation.ui.components.AreaDropdown
import com.performily.flowboard.features.workspace.presentation.ui.components.OrganizationChartNodeItem
import com.performily.flowboard.features.workspace.presentation.ui.components.PendingNodeCard
import com.performily.flowboard.features.workspace.presentation.ui.components.SectionTitle
import com.performily.flowboard.features.workspace.presentation.viewmodel.OrganizationChartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationChartScreen(
    onBack: () -> Unit,
    areaId: Long? = null,
    highlightedEmployeeId: Long? = null,
    viewModel: OrganizationChartViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.initialize(areaId, highlightedEmployeeId)
    }
    val modes = listOf(
        OrganizationChartMode.WHOLE_ORGANIZATION to "Toda la organización",
        OrganizationChartMode.BY_AREA to "Por área"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organigrama") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                modes.forEachIndexed { index, (mode, label) ->
                    SegmentedButton(
                        selected = state.mode == mode,
                        onClick = { viewModel.onModeChange(mode) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = modes.size)
                    ) {
                        Text(label)
                    }
                }
            }

            if (state.mode == OrganizationChartMode.BY_AREA) {
                AreaDropdown(
                    areas = state.areas,
                    selectedAreaId = state.selectedAreaId,
                    onSelect = { viewModel.onAreaSelected(it.id) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            val chart = state.chart
            when {
                state.isLoading && chart == null -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    Text(state.errorMessage.orEmpty(), color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = viewModel::loadChart) { Text("Reintentar") }
                }

                chart == null || chart.isEmpty -> {
                    Text(
                        text = "No hay colaboradores activos para mostrar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> {
                    chart.nodes.forEach { node ->
                        OrganizationChartNodeItem(node, highlightedEmployeeId = state.highlightedEmployeeId)
                    }
                    if (chart.pendingReassignment.isNotEmpty()) {
                        SectionTitle("Pendientes de reasignación")
                        chart.pendingReassignment.forEach { node -> PendingNodeCard(node) }
                    }
                    Text(
                        text = "Los colaboradores sin jefe directo aparecen en el primer nivel.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
