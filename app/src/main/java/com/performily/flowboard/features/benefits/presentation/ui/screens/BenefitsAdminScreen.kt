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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
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
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

private const val CATALOG_TAB = 0

private val AdminTabs = listOf("Catálogo", "Asignaciones")

private val AssignmentFilters = listOf(AssignmentStatus.ASSIGNED to "Por entregar", AssignmentStatus.DELIVERED to "Entregados")

/**
 * Benefits screen for HR (MA-60 / MA-62).
 *
 * Shows the catalog of benefit types and the assignments with their delivery registration. The
 * tabs and the assignment filters slide away while the list scrolls down. The top bar opens the
 * vacation balances.
 *
 * @param onBack navigates back.
 * @param onAssignClick opens "Asignar beneficio".
 * @param onVacationBalancesClick opens the vacation balances.
 * @param resultMessage message left by "Asignar beneficio" when it returns.
 * @param onResultMessageConsumed called once [resultMessage] has been handled.
 * @param viewModel view model of the screen.
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
    val headerState = rememberCollapsingHeaderState()

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

    val isCatalog = state.selectedTab == CATALOG_TAB

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Beneficios",
                onBack = onBack,
                actions = {
                    TextButton(onClick = onVacationBalancesClick) { Text("Vacaciones") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text(if (isCatalog) "Nuevo beneficio" else "Asignar beneficio") },
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                onClick = if (isCatalog) viewModel::showTypeSheet else onAssignClick,
                expanded = headerState.isHeaderVisible,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(
                state.selectedTab,
                state.assignmentFilter,
                state.benefitTypes.size,
                state.assignments.size,
                state.catalogError != null,
                state.assignmentsError != null
            ),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.screenHorizontal)
                        .padding(top = 4.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    FlowboardTabs(
                        labels = AdminTabs,
                        selectedIndex = state.selectedTab,
                        onSelect = { viewModel.onTabSelected(it) }
                    )
                    if (!isCatalog) {
                        AssignmentFilterRow(
                            selected = state.assignmentFilter,
                            onFilterSelected = viewModel::onFilterSelected
                        )
                    }
                }
            }
        ) { contentPadding ->
            if (isCatalog) {
                CatalogTab(
                    state = state,
                    contentPadding = contentPadding,
                    onTypeClick = viewModel::requestToggleType,
                    onRetry = viewModel::load
                )
            } else {
                AssignmentsTab(
                    state = state,
                    contentPadding = contentPadding,
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

/**
 * List padding that leaves room for the collapsing header and the floating button.
 *
 * @param contentPadding padding given by the collapsing header layout.
 */
private fun listPadding(contentPadding: PaddingValues): PaddingValues = PaddingValues(
    top = contentPadding.calculateTopPadding() + 8.dp,
    bottom = Spacing.fabClearance,
    start = Spacing.screenHorizontal,
    end = Spacing.screenHorizontal
)

/**
 * Filter chips of the assignments tab: "Por entregar" and "Entregados".
 *
 * @param selected filter currently applied.
 * @param onFilterSelected called with the filter the user taps.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssignmentFilterRow(selected: AssignmentStatus?, onFilterSelected: (AssignmentStatus) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        AssignmentFilters.forEach { (status, label) ->
            val isSelected = selected == status
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(status) },
                label = { Text(label) },
                leadingIcon = if (isSelected) {
                    { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else {
                    null
                }
            )
        }
    }
}

/**
 * Catalog tab (MA-60). Tapping a type lets the user activate or deactivate it.
 *
 * @param state screen state.
 * @param contentPadding padding given by the collapsing header layout.
 * @param onTypeClick called with the tapped type.
 * @param onRetry reloads the data.
 */
@Composable
private fun CatalogTab(
    state: BenefitsAdminUiState,
    contentPadding: PaddingValues,
    onTypeClick: (BenefitType) -> Unit,
    onRetry: () -> Unit
) {
    val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    when {
        state.isLoadingCatalog -> BenefitsLoading(modifier = stateModifier)
        state.catalogError != null && state.benefitTypes.isEmpty() -> BenefitsMessageState(
            title = "No se pudo cargar el catálogo",
            message = state.catalogError,
            modifier = stateModifier,
            actionLabel = "Reintentar",
            onAction = onRetry
        )
        state.benefitTypes.isEmpty() -> BenefitsMessageState(
            title = "El catálogo está vacío",
            message = "Crea el primer tipo de beneficio con el botón \"Nuevo beneficio\".",
            modifier = stateModifier
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding(contentPadding)
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

/** Detail line of a catalog type: its unit plus "Maneja saldo" or its description. */
private fun catalogDetail(type: BenefitType): String =
    listOfNotNull(type.unit.label, if (type.hasBalance) "Maneja saldo" else type.description).joinToString(" · ")

/**
 * Assignments tab (MA-62): assignments to deliver or already delivered. Tapping one to deliver
 * opens "Registrar entrega".
 *
 * @param state screen state.
 * @param contentPadding padding given by the collapsing header layout.
 * @param onAssignmentClick called with the tapped assignment.
 * @param onRetry reloads the data.
 */
@Composable
private fun AssignmentsTab(
    state: BenefitsAdminUiState,
    contentPadding: PaddingValues,
    onAssignmentClick: (BenefitAssignment) -> Unit,
    onRetry: () -> Unit
) {
    val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    when {
        state.isLoadingAssignments -> BenefitsLoading(modifier = stateModifier)
        state.assignmentsError != null && state.assignments.isEmpty() -> BenefitsMessageState(
            title = "No se pudieron cargar las asignaciones",
            message = state.assignmentsError,
            modifier = stateModifier,
            actionLabel = "Reintentar",
            onAction = onRetry
        )
        state.assignments.isEmpty() -> BenefitsMessageState(
            title = if (state.assignmentFilter == AssignmentStatus.ASSIGNED) "No hay beneficios por entregar" else "Aún no hay entregas",
            message = if (state.assignmentFilter == AssignmentStatus.ASSIGNED) {
                "Asigna un beneficio a un colaborador o a un área con el botón \"Asignar beneficio\"."
            } else {
                "Las entregas registradas aparecerán aquí."
            },
            modifier = stateModifier
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding(contentPadding)
        ) {
            items(state.assignments, key = { it.id }) { assignment ->
                AssignmentRow(assignment = assignment, onClick = { onAssignmentClick(assignment) })
            }
        }
    }
}

/**
 * Row of one assignment: employee, benefit, quantity and, once delivered, the delivery date.
 *
 * @param assignment assignment to show.
 * @param onClick called when the row is tapped; only active while it can still be delivered.
 */
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
