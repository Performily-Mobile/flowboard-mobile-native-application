package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.presentation.ui.components.DocumentsTab
import com.performily.flowboard.features.workspace.presentation.ui.components.EmployeeAvatar
import com.performily.flowboard.features.workspace.presentation.ui.components.JobAssignmentTab
import com.performily.flowboard.features.workspace.presentation.ui.components.PersonalDataTab
import com.performily.flowboard.features.workspace.presentation.ui.components.StatusChip
import com.performily.flowboard.features.workspace.presentation.viewmodel.EmployeeDetailViewModel

private val tabs = listOf("Datos", "Puesto", "Expediente", "Cuenta")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employeeId: Long,
    onBack: () -> Unit,
    viewModel: EmployeeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(employeeId) {
        viewModel.load(employeeId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ficha del colaborador") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val employee = state.employee
        when {
            state.isLoading && employee == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            employee == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(state.errorMessage ?: "No se encontró el colaborador.")
                    OutlinedButton(onClick = { viewModel.load(employeeId) }) { Text("Reintentar") }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    EmployeeHeader(employee)
                    TabRow(
                        selectedTabIndex = state.selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = state.selectedTab == index,
                                onClick = { viewModel.onTabSelected(index) },
                                text = { Text(title) }
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        when (state.selectedTab) {
                            0 -> PersonalDataTab(employee)
                            1 -> JobAssignmentTab(
                                employee = employee,
                                directManagerName = state.directManagerName,
                                jobAssignments = state.jobAssignments
                            )
                            2 -> DocumentsTab(
                                documents = state.filteredDocuments,
                                selectedCategory = state.documentFilter,
                                onCategoryChange = viewModel::onDocumentFilterChange
                            )
                            else -> Text(
                                text = "La cuenta de acceso y el rol del colaborador se gestionan en el módulo de identidad (IAM).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeHeader(employee: Employee) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EmployeeAvatar(initials = employee.name.initials, size = 56.dp)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = employee.name.fullName, style = MaterialTheme.typography.titleMedium)
            Text(
                text = employee.jobDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusChip(status = employee.status)
        }
    }
}
