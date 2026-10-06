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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.presentation.state.EmployeeAction
import com.performily.flowboard.features.workspace.presentation.ui.components.DocumentsTab
import com.performily.flowboard.features.workspace.presentation.ui.components.EmployeeAvatar
import com.performily.flowboard.features.workspace.presentation.ui.components.JobAssignmentTab
import com.performily.flowboard.features.workspace.presentation.ui.components.PersonalDataTab
import com.performily.flowboard.features.workspace.presentation.ui.components.ReassignJobSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.ReinstateEmployeeSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.StatusChip
import com.performily.flowboard.features.workspace.presentation.ui.components.TerminateEmployeeSheet
import com.performily.flowboard.features.workspace.presentation.ui.components.TerminationBlockedDialog
import com.performily.flowboard.features.workspace.presentation.ui.components.UploadDocumentSheet
import com.performily.flowboard.features.workspace.presentation.viewmodel.EmployeeDetailViewModel

private val tabs = listOf("Datos", "Puesto", "Expediente", "Cuenta")
private const val DOCUMENTS_TAB = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDetailScreen(
    employeeId: Long,
    onBack: () -> Unit,
    onEditPersonalData: (Long) -> Unit,
    onOpenEmployee: (employeeId: Long, openReassign: Boolean) -> Unit,
    openReassignOnStart: Boolean = false,
    viewModel: EmployeeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var reassignHandled by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(employeeId) {
        viewModel.load(employeeId)
    }

    // Llegamos desde "Reasignar reportes": abrimos la hoja apenas cargue la ficha.
    LaunchedEffect(state.employee != null) {
        if (openReassignOnStart && !reassignHandled && state.employee != null) {
            reassignHandled = true
            viewModel.openReassignJob()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.consumeMessage()
        }
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
                actions = {
                    state.employee?.let { employee ->
                        EmployeeMenu(
                            employee = employee,
                            onEditPersonalData = { onEditPersonalData(employee.id.value) },
                            onReassignJob = viewModel::openReassignJob,
                            onUploadDocument = viewModel::openUploadDocument,
                            onTerminate = viewModel::openTerminate,
                            onReinstate = viewModel::openReinstate
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
                            DOCUMENTS_TAB -> DocumentsTab(
                                documents = state.filteredDocuments,
                                selectedCategory = state.documentFilter,
                                onCategoryChange = viewModel::onDocumentFilterChange,
                                onUploadClick = viewModel::openUploadDocument
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

    val employee = state.employee ?: return
    when (state.activeAction) {
        EmployeeAction.REASSIGN_JOB -> ReassignJobSheet(
            employeeName = employee.name.fullName,
            form = state.reassignJobForm,
            areas = state.areas,
            positions = state.positionsOf(state.reassignJobForm.areaId),
            managerCandidates = state.managerCandidates,
            isSaving = state.isSaving,
            errorMessage = state.actionError,
            onAreaChange = viewModel::onReassignAreaChange,
            onPositionChange = viewModel::onReassignPositionChange,
            onManagerChange = viewModel::onReassignManagerChange,
            onDateChange = viewModel::onReassignDateChange,
            onSave = viewModel::saveReassignJob,
            onDismiss = viewModel::dismissAction
        )

        EmployeeAction.TERMINATE -> TerminateEmployeeSheet(
            employeeName = employee.name.fullName,
            form = state.terminationForm,
            isSaving = state.isSaving,
            errorMessage = state.actionError,
            onReasonChange = viewModel::onTerminationReasonChange,
            onDateChange = viewModel::onTerminationDateChange,
            onConfirm = viewModel::saveTermination,
            onDismiss = viewModel::dismissAction
        )

        EmployeeAction.TERMINATION_BLOCKED -> TerminationBlockedDialog(
            employeeName = employee.name.fullName,
            subordinates = state.subordinates,
            onSubordinateClick = { subordinate ->
                viewModel.dismissAction()
                onOpenEmployee(subordinate.id.value, false)
            },
            onReassignReports = {
                viewModel.dismissAction()
                state.subordinates.firstOrNull()?.let { onOpenEmployee(it.id.value, true) }
            },
            onDismiss = viewModel::dismissAction
        )

        EmployeeAction.REINSTATE -> ReinstateEmployeeSheet(
            employeeName = employee.name.fullName,
            form = state.reinstateForm,
            areas = state.areas,
            positions = state.positionsOf(state.reinstateForm.areaId),
            isSaving = state.isSaving,
            errorMessage = state.actionError,
            onAreaChange = viewModel::onReinstateAreaChange,
            onPositionChange = viewModel::onReinstatePositionChange,
            onDateChange = viewModel::onReinstateDateChange,
            onSave = viewModel::saveReinstate,
            onDismiss = viewModel::dismissAction
        )

        EmployeeAction.UPLOAD_DOCUMENT -> UploadDocumentSheet(
            form = state.uploadDocumentForm,
            isSaving = state.isSaving,
            errorMessage = state.actionError,
            onDocumentTypeChange = viewModel::onUploadDocumentTypeChange,
            onFileSelected = viewModel::onFileSelected,
            onSave = viewModel::saveDocument,
            onDismiss = viewModel::dismissAction
        )

        EmployeeAction.NONE -> Unit
    }
}

@Composable
private fun EmployeeMenu(
    employee: Employee,
    onEditPersonalData: () -> Unit,
    onReassignJob: () -> Unit,
    onUploadDocument: () -> Unit,
    onTerminate: () -> Unit,
    onReinstate: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(FlowboardIcons.MoreVert, contentDescription = "Más opciones")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MenuItem("Editar datos personales") { expanded = false; onEditPersonalData() }
            if (!employee.isTerminated) {
                MenuItem("Reasignar puesto") { expanded = false; onReassignJob() }
            }
            MenuItem("Subir documento") { expanded = false; onUploadDocument() }
            if (employee.isTerminated) {
                MenuItem("Reincorporar") { expanded = false; onReinstate() }
            } else {
                MenuItem("Registrar cese") { expanded = false; onTerminate() }
            }
        }
    }
}

@Composable
private fun MenuItem(text: String, onClick: () -> Unit) {
    DropdownMenuItem(text = { Text(text) }, onClick = onClick)
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
