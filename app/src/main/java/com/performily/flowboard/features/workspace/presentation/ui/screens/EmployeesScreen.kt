package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Area
import com.performily.flowboard.features.workspace.domain.entity.Position
import com.performily.flowboard.features.workspace.presentation.state.EmployeesUiState
import com.performily.flowboard.features.workspace.presentation.ui.components.EmployeeListItem
import com.performily.flowboard.features.workspace.presentation.ui.components.label
import com.performily.flowboard.features.workspace.presentation.viewmodel.EmployeesViewModel
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus

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

    LaunchedEffect(Unit) {
        viewModel.loadEmployees()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Colaboradores") },
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onRegisterClick,
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                text = { Text("Nuevo colaborador") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("Buscar por nombre, apellido o DNI") },
                leadingIcon = { Icon(FlowboardIcons.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(FlowboardIcons.Close, contentDescription = "Limpiar búsqueda")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
            EmployeeFilters(
                state = state,
                onAreaSelected = viewModel::onAreaSelected,
                onStatusSelected = viewModel::onStatusSelected,
                onPositionSelected = viewModel::onPositionSelected
            )
            Spacer(modifier = Modifier.height(10.dp))

            when {
                state.isLoading && state.employees.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage != null -> {
                    MessageState(
                        title = "No se pudo cargar la lista",
                        message = state.errorMessage.orEmpty(),
                        actionLabel = "Reintentar",
                        onAction = viewModel::loadEmployees
                    )
                }

                state.employees.isEmpty() -> {
                    MessageState(
                        title = "No encontramos colaboradores",
                        message = emptyMessage(state),
                        actionLabel = if (state.hasFilters) "Limpiar filtros" else null,
                        onAction = viewModel::clearFilters
                    )
                }

                else -> {
                    Text(
                        text = "${state.employees.size} colaboradores · orden alfabético por apellido",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(contentPadding = PaddingValues(bottom = 88.dp)) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmployeeFilters(
    state: EmployeesUiState,
    onAreaSelected: (Area?) -> Unit,
    onStatusSelected: (EmploymentStatus?) -> Unit,
    onPositionSelected: (Position?) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
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

@Composable
private fun MessageState(
    title: String,
    message: String,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
