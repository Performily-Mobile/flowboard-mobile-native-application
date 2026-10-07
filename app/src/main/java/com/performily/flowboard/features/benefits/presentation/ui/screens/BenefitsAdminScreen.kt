package com.performily.flowboard.features.benefits.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.benefits.domain.entity.BenefitAssignment
import com.performily.flowboard.features.benefits.domain.entity.BenefitType
import com.performily.flowboard.features.benefits.domain.valueobject.AssignmentStatus
import com.performily.flowboard.features.benefits.presentation.state.BenefitsAdminUiState
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitChip
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitListRow
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsColors
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsFormatters
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsLoading
import com.performily.flowboard.features.benefits.presentation.ui.components.BenefitsMessageState
import com.performily.flowboard.features.benefits.presentation.ui.components.CreateBenefitTypeSheet
import com.performily.flowboard.features.benefits.presentation.ui.components.IconCircle
import com.performily.flowboard.features.benefits.presentation.ui.components.InitialsAvatar
import com.performily.flowboard.features.benefits.presentation.ui.components.RegisterDeliveryDialog
import com.performily.flowboard.features.benefits.presentation.ui.components.ToggleBenefitTypeDialog
import com.performily.flowboard.features.benefits.presentation.viewmodel.BenefitsAdminViewModel

private const val CATALOG_TAB = 0

/**
 * MA-60 / MA-62 · Beneficios (RR.HH.): catálogo de tipos y asignaciones con el
 * registro de entregas. Desde la barra superior se abren los saldos de vacaciones.
 *
 * @param resultMessage mensaje que deja "Asignar beneficio" al volver
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BenefitsAdminScreen(
    onBack: () -> Unit,
    onAssignClick: () -> Unit,
    onVacationBalancesClick: () -> Unit,
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    viewModel: BenefitsAdminViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            viewModel.onAssignmentResult(it)
            onResultMessageConsumed()
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Beneficios") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    TextButton(onClick = onVacationBalancesClick) { Text("Vacaciones") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            val isCatalog = state.selectedTab == CATALOG_TAB
            ExtendedFloatingActionButton(
                onClick = if (isCatalog) viewModel::showTypeSheet else onAssignClick,
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                text = { Text(if (isCatalog) "Nuevo beneficio" else "Asignar beneficio") },
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
                listOf("Catálogo", "Asignaciones").forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.onTabSelected(index) },
                        text = { Text(title) }
                    )
                }
            }
            if (state.selectedTab == CATALOG_TAB) {
                CatalogTab(state = state, onTypeClick = viewModel::requestToggleType, onRetry = viewModel::load)
            } else {
                AssignmentsTab(
                    state = state,
                    onFilterSelected = viewModel::onFilterSelected,
                    onAssignmentClick = viewModel::showDeliveryDialog,
                    onRetry = viewModel::load
                )
            }
        }
    }

    if (state.isTypeSheetVisible) {
        CreateBenefitTypeSheet(
            form = state.typeForm,
            onNameChange = viewModel::onTypeNameChange,
            onUnitChange = viewModel::onTypeUnitChange,
            onHasBalanceChange = viewModel::onTypeHasBalanceChange,
            onCreate = viewModel::saveType,
            onDismiss = viewModel::dismissTypeSheet
        )
    }

    state.typeToToggle?.let { type ->
        ToggleBenefitTypeDialog(
            benefitType = type,
            isSaving = state.isTogglingType,
            onConfirm = viewModel::confirmToggleType,
            onDismiss = viewModel::dismissToggleType
        )
    }

    state.deliveryForm?.let { form ->
        RegisterDeliveryDialog(
            form = form,
            onDateChange = viewModel::onDeliveryDateChange,
            onNotesChange = viewModel::onDeliveryNotesChange,
            onConfirm = viewModel::confirmDelivery,
            onDismiss = viewModel::dismissDeliveryDialog
        )
    }
}

/** MA-60 · Catálogo. Tocar un tipo permite activarlo o desactivarlo. */
@Composable
private fun CatalogTab(state: BenefitsAdminUiState, onTypeClick: (BenefitType) -> Unit, onRetry: () -> Unit) {
    when {
        state.isLoadingCatalog -> BenefitsLoading()
        state.catalogError != null && state.benefitTypes.isEmpty() -> BenefitsMessageState(
            title = "No se pudo cargar el catálogo",
            message = state.catalogError,
            actionLabel = "Reintentar",
            onAction = onRetry
        )
        state.benefitTypes.isEmpty() -> BenefitsMessageState(
            title = "El catálogo está vacío",
            message = "Crea el primer tipo de beneficio con el botón \"Nuevo beneficio\"."
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
        ) {
            items(state.benefitTypes, key = { it.id }) { type ->
                BenefitListRow(
                    title = type.name,
                    subtitle = catalogDetail(type),
                    onClick = { onTypeClick(type) },
                    leading = { IconCircle(icon = FlowboardIcons.Gift) },
                    trailing = {
                        BenefitChip(
                            text = if (type.active) "Activo" else "Inactivo",
                            containerColor = if (type.active) BenefitsColors.Active else BenefitsColors.Inactive
                        )
                    }
                )
            }
        }
    }
}

/** "Monto (S/) · Mensual", "Días · Maneja saldo": unidad más saldo o descripción. */
private fun catalogDetail(type: BenefitType): String =
    listOfNotNull(type.unit.label, if (type.hasBalance) "Maneja saldo" else type.description).joinToString(" · ")

/** MA-62 · Asignaciones por entregar o entregadas. Tocar una por entregar abre "Registrar entrega". */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentsTab(
    state: BenefitsAdminUiState,
    onFilterSelected: (AssignmentStatus) -> Unit,
    onAssignmentClick: (BenefitAssignment) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(AssignmentStatus.ASSIGNED to "Por entregar", AssignmentStatus.DELIVERED to "Entregados")
                .forEach { (status, label) ->
                    val selected = state.assignmentFilter == status
                    FilterChip(
                        selected = selected,
                        onClick = { onFilterSelected(status) },
                        label = { Text(label) },
                        leadingIcon = if (selected) {
                            { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        }
                    )
                }
        }
        when {
            state.isLoadingAssignments -> BenefitsLoading()
            state.assignmentsError != null && state.assignments.isEmpty() -> BenefitsMessageState(
                title = "No se pudieron cargar las asignaciones",
                message = state.assignmentsError,
                actionLabel = "Reintentar",
                onAction = onRetry
            )
            state.assignments.isEmpty() -> BenefitsMessageState(
                title = if (state.assignmentFilter == AssignmentStatus.ASSIGNED) "No hay beneficios por entregar" else "Aún no hay entregas",
                message = if (state.assignmentFilter == AssignmentStatus.ASSIGNED) {
                    "Asigna un beneficio a un colaborador o a un área con el botón \"Asignar beneficio\"."
                } else {
                    "Las entregas registradas aparecerán aquí."
                }
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 96.dp)
            ) {
                items(state.assignments, key = { it.id }) { assignment ->
                    AssignmentRow(assignment = assignment, onClick = { onAssignmentClick(assignment) })
                }
            }
        }
    }
}

@Composable
private fun AssignmentRow(assignment: BenefitAssignment, onClick: () -> Unit) {
    val quantity = "${assignment.benefitTypeName} · ${BenefitsFormatters.quantity(assignment.quantity, assignment.unit)}"
    val deliveredOn = assignment.delivery?.deliveredOn
    BenefitListRow(
        title = assignment.employeeName,
        subtitle = if (deliveredOn != null) "$quantity · ${BenefitsFormatters.date(deliveredOn)}" else quantity,
        onClick = if (assignment.canBeDelivered) onClick else null,
        leading = { InitialsAvatar(name = assignment.employeeName) },
        trailing = { BenefitChip(assignment.status.label, BenefitsColors.status(assignment.status)) }
    )
}
