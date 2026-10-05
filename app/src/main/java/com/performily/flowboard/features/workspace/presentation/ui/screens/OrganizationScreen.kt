package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.presentation.ui.components.AreasTab
import com.performily.flowboard.features.workspace.presentation.ui.components.CreateAreaSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.CreatePositionSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.PositionsTab
import com.performily.flowboard.features.workspace.presentation.viewmodel.OrganizationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationScreen(
    onBack: () -> Unit,
    viewModel: OrganizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Organización") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = if (state.selectedTab == 0) viewModel::showAreaSheet else viewModel::showPositionSheet,
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                text = { Text(if (state.selectedTab == 0) "Nueva área" else "Nueva posición") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = state.selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                Tab(selected = state.selectedTab == 0, onClick = { viewModel.onTabSelected(0) }, text = { Text("Áreas") })
                Tab(selected = state.selectedTab == 1, onClick = { viewModel.onTabSelected(1) }, text = { Text("Posiciones") })
            }
            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            if (state.isLoading && state.areas.isEmpty() && state.positions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.selectedTab == 0) {
                AreasTab(areas = state.areas)
            } else {
                PositionsTab(positions = state.positions)
            }
        }
    }

    if (state.isAreaSheetVisible) {
        CreateAreaSheet(
            form = state.areaForm,
            isSaving = state.isSaving,
            onNameChange = viewModel::onAreaNameChange,
            onDescriptionChange = viewModel::onAreaDescriptionChange,
            onSave = viewModel::saveArea,
            onDismiss = viewModel::dismissSheets
        )
    }

    if (state.isPositionSheetVisible) {
        CreatePositionSheet(
            form = state.positionForm,
            areas = state.activeAreas,
            isSaving = state.isSaving,
            onAreaChange = viewModel::onPositionAreaChange,
            onTitleChange = viewModel::onPositionTitleChange,
            onSalaryChange = viewModel::onPositionSalaryChange,
            onSave = viewModel::savePosition,
            onDismiss = viewModel::dismissSheets
        )
    }
}
