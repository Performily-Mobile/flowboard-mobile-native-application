package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.presentation.ui.components.AreasTab
import com.performily.flowboard.features.workspace.presentation.ui.components.CreateAreaSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.CreatePositionSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.DeactivateAreaDialog
import com.performily.flowboard.features.workspace.presentation.ui.components.PositionsTab
import com.performily.flowboard.features.workspace.presentation.viewmodel.OrganizationViewModel

/**
 * Areas and positions of the organization, in two tabs, with sheets to create them.
 *
 * @param onBack called by the back button.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizationScreen(
    onBack: () -> Unit,
    viewModel: OrganizationViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val headerState = rememberCollapsingHeaderState()

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
                text = { Text(if (state.selectedTab == 0) "Nueva área" else "Nueva posición") },
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                onClick = if (state.selectedTab == 0) viewModel::showAreaSheet else viewModel::showPositionSheet,
                expanded = headerState.isHeaderVisible,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.selectedTab, state.areas.size, state.positions.size),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    FlowboardTabs(
                        labels = listOf("Áreas", "Posiciones"),
                        selectedIndex = state.selectedTab,
                        onSelect = { viewModel.onTabSelected(it) }
                    )
                    state.errorMessage?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        ) { contentPadding ->
            val listPadding = PaddingValues(
                top = contentPadding.calculateTopPadding() + 4.dp,
                bottom = Spacing.fabClearance,
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal
            )
            if (state.isLoading && state.areas.isEmpty() && state.positions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = contentPadding.calculateTopPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.selectedTab == 0) {
                AreasTab(
                    areas = state.areas,
                    onDeactivateClick = viewModel::requestDeactivateArea,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding
                )
            } else {
                PositionsTab(
                    positions = state.positions,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = listPadding
                )
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

    state.areaToDeactivate?.let { area ->
        DeactivateAreaDialog(
            area = area,
            confirmed = state.deactivationConfirmed,
            isSaving = state.isSaving,
            errorMessage = state.deactivationError,
            onConfirmedChange = viewModel::onDeactivationConfirmedChange,
            onConfirm = viewModel::confirmDeactivateArea,
            onDismiss = viewModel::dismissDeactivateArea
        )
    }
}
