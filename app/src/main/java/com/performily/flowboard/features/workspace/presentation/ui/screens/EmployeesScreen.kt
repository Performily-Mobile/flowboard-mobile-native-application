package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardSearchField
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.presentation.state.EmployeesUiState
import com.performily.flowboard.features.workspace.presentation.ui.components.EmployeeListItem
import com.performily.flowboard.features.workspace.presentation.ui.components.label
import com.performily.flowboard.features.workspace.presentation.viewmodel.EmployeesViewModel
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * List of employees with a search box and filters that slide away while scrolling down.
 *
 * @param onEmployeeClick called with the id of the tapped employee.
 * @param onRegisterClick called by the "Nuevo colaborador" button.
 * @param onOrganizationClick opens areas and positions.
 * @param onOrganizationChartClick opens the organization chart.
 * @param viewModel view model of the screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeesScreen(
    onEmployeeClick: (Long) -> Unit,
    onRegisterClick: () -> Unit,
    onOrganizationClick: () -> Unit,
    onOrganizationChartClick: () -> Unit,
    viewModel: EmployeesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    val headerState = rememberCollapsingHeaderState()

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Colaboradores",
                actions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(FlowboardIcons.MoreVert, contentDescription = "Más opciones")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Áreas y posiciones") },
                                onClick = {
                                    menuExpanded = false
                                    onOrganizationClick()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Organigrama") },
                                onClick = {
                                    menuExpanded = false
                                    onOrganizationChartClick()
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nuevo colaborador") },
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                onClick = onRegisterClick,
                expanded = headerState.isHeaderVisible,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(state.employees.size, state.errorMessage != null),
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
                        placeholder = "Buscar por nombre, apellido o DNI"
                    )
                    EmployeeFilters(
                        state = state,
                        onAreaSelected = viewModel::onAreaSelected,
                        onStatusSelected = viewModel::onStatusSelected,
                        onPositionSelected = viewModel::onPositionSelected
                    )
                }
            }
        ) { contentPadding ->
            when {
                state.isLoading && state.employees.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = contentPadding.calculateTopPadding()),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    MessageState(
                        title = "No se pudo cargar la lista",
                        message = state.errorMessage.orEmpty(),
                        actionLabel = "Reintentar",
                        onAction = viewModel::loadEmployees,
                        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                    )
                }

                state.employees.isEmpty() -> {
                    MessageState(
                        title = "No encontramos colaboradores",
                        message = emptyMessage(state),
                        actionLabel = if (state.hasFilters) "Limpiar filtros" else null,
                        onAction = viewModel::clearFilters,
                        modifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            top = contentPadding.calculateTopPadding() + 4.dp,
                            bottom = Spacing.fabClearance,
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                    ) {
                        item(key = "count") {
                            Text(
                                text = "${state.employees.size} colaboradores",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                        items(state.employees, key = { it.id.value }) { employee ->
                            EmployeeListItem(
                                employee = employee,
                                onClick = { onEmployeeClick(employee.id.value) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Single row of filter chips (area, status, position) that scrolls sideways when it does not fit. */
@Composable
private fun EmployeeFilters(
    state: EmployeesUiState,
    onAreaSelected: (Area?) -> Unit,
    onStatusSelected: (EmploymentStatus?) -> Unit,
    onPositionSelected: (Position?) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
    ) {
        FilterMenuChip(
            name = "Área",
            selectedLabel = state.selectedArea?.name,
            options = state.areas,
            optionLabel = { it.name },
            onSelect = { onAreaSelected(it) },
            onClear = { onAreaSelected(null) }
        )
        FilterMenuChip(
            name = "Estado",
            selectedLabel = state.selectedStatus?.label(),
            options = EmploymentStatus.entries,
            optionLabel = { it.label() },
            onSelect = { onStatusSelected(it) },
            onClear = { onStatusSelected(null) }
        )
        FilterMenuChip(
            name = "Puesto",
            selectedLabel = state.selectedPosition?.title,
            options = state.positionsForFilter,
            optionLabel = { it.title },
            onSelect = { onPositionSelected(it) },
            onClear = { onPositionSelected(null) }
        )
    }
}

/**
 * Filter chip that opens a menu of options; when a value is selected, tapping it clears the filter.
 */
@Composable
private fun <T> FilterMenuChip(
    name: String,
    selectedLabel: String?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedLabel != null
    Box {
        FilterChip(
            selected = selected,
            onClick = { if (selected) onClear() else expanded = true },
            label = { Text(if (selected) "$name: $selectedLabel" else name) },
            leadingIcon = if (selected) {
                { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else {
                null
            },
            trailingIcon = {
                Icon(
                    imageVector = if (selected) FlowboardIcons.Close else FlowboardIcons.ArrowDropDown,
                    contentDescription = if (selected) "Quitar filtro" else null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

/** Centered title, message and optional action shown for the error and empty states. */
@Composable
private fun MessageState(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = Spacing.screenHorizontal, end = Spacing.screenHorizontal),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** Builds the empty-state message from the active search and filters. */
private fun emptyMessage(state: EmployeesUiState): String {
    if (!state.hasFilters) return "Todavía no hay colaboradores registrados."
    val parts = buildList {
        if (state.query.isNotBlank()) add("coincide con \"${state.query}\"")
        state.selectedArea?.let { add("en ${it.name}") }
        state.selectedPosition?.let { add("con puesto ${it.title}") }
        state.selectedStatus?.let { add("con estado ${it.label()}") }
    }
    return "Ningún registro " + parts.joinToString(" ") + "."
}
