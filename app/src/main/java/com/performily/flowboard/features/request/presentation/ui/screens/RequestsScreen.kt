package com.performily.flowboard.features.request.presentation.ui.screens

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType
import com.performily.flowboard.features.request.presentation.state.AllRequestsFilter
import com.performily.flowboard.features.request.presentation.state.ApprovalSort
import com.performily.flowboard.features.request.presentation.state.MyRequestsFilter
import com.performily.flowboard.features.request.presentation.state.RequestsTab
import com.performily.flowboard.features.request.presentation.state.RequestsUiState
import com.performily.flowboard.features.request.presentation.ui.components.ApprovalCard
import com.performily.flowboard.features.request.presentation.ui.components.MyRequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RejectRequestDialog
import com.performily.flowboard.features.request.presentation.ui.components.RequestBanner
import com.performily.flowboard.features.request.presentation.ui.components.RequestColors
import com.performily.flowboard.features.request.presentation.ui.components.RequestFormatters
import com.performily.flowboard.features.request.presentation.ui.components.RequestListRow
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.ui.components.RequestSegmented
import com.performily.flowboard.features.request.presentation.viewmodel.RequestsViewModel


/**
 * Requests screen with the viewer's own requests, the requests to approve, the HR inbox and all requests.
 *
 * The tabs and the filters of the selected tab live in a header that slides away while the list
 * scrolls down and comes back when it scrolls up.
 *
 * @param onNewRequest called when "Nueva solicitud" is tapped.
 * @param onMyRequestClick called with the identifier of one of the viewer's requests.
 * @param onReviewClick called with the identifier of a request to review.
 * @param onResolvedClick called when the resolved requests are asked for.
 * @param resultMessage message left by a previous screen, shown in a snackbar.
 * @param onResultMessageConsumed called once [resultMessage] has been handled.
 * @param viewModel requests view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    onNewRequest: () -> Unit,
    onMyRequestClick: (requestId: Long) -> Unit,
    onReviewClick: (requestId: Long) -> Unit,
    onResolvedClick: () -> Unit,
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    viewModel: RequestsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val headerState = rememberCollapsingHeaderState()

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            viewModel.onResult(it)
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
                title = { Text("Solicitudes") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.selectedTab == RequestsTab.MINE) {
                ExtendedFloatingActionButton(
                    text = { Text("Nueva solicitud") },
                    icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                    onClick = onNewRequest,
                    expanded = headerState.isHeaderVisible,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { paddingValues ->
        CollapsingHeaderLayout(
            modifier = Modifier.padding(paddingValues),
            state = headerState,
            showHeaderKey = listOf(
                state.selectedTab,
                state.visibleMyRequests.size,
                state.visiblePending.size,
                state.visibleAll.size,
                state.isLoadingMine || state.isLoadingPending || state.isLoadingAll,
                state.mineError != null || state.pendingError != null || state.allError != null
            ),
            header = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = Spacing.m),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    RequestSegmented(
                        options = state.tabs,
                        selected = state.selectedTab,
                        optionLabel = state::tabLabel,
                        onSelect = viewModel::onTabSelected,
                        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = 4.dp)
                    )
                    when (state.selectedTab) {
                        RequestsTab.MINE -> ChipRow {
                            MyRequestsFilter.entries.forEach { filter ->
                                SelectableChip(
                                    label = filter.label,
                                    selected = state.myFilter == filter,
                                    onClick = { viewModel.onMyFilterSelected(filter) }
                                )
                            }
                        }
                        RequestsTab.TO_APPROVE -> ChipRow {
                            DropdownChip(
                                label = "Tipo: ${state.typeFilter?.name ?: "todos"}",
                                selected = state.typeFilter != null,
                                options = listOf<RequestType?>(null) + state.requestTypes,
                                optionLabel = { it?.name ?: "Todos" },
                                onSelect = viewModel::onTypeFilterSelected
                            )
                            DropdownChip(
                                label = state.sort.label,
                                selected = state.pending.isNotEmpty(),
                                options = ApprovalSort.entries,
                                optionLabel = { it.label },
                                onSelect = viewModel::onSortSelected
                            )
                        }
                        RequestsTab.HR_INBOX -> RequestBanner(
                            message = "Aquí llegan las solicitudes de colaboradores que no tienen jefe directo asignado.",
                            icon = FlowboardIcons.Info,
                            containerColor = RequestColors.InfoBanner,
                            contentColor = RequestColors.OnInfoBanner,
                            modifier = Modifier.padding(horizontal = Spacing.screenHorizontal)
                        )
                        RequestsTab.ALL -> ChipRow {
                            AllRequestsFilter.entries.forEach { filter ->
                                SelectableChip(
                                    label = filter.label,
                                    selected = state.allFilter == filter,
                                    onClick = { viewModel.onAllFilterSelected(filter) }
                                )
                            }
                        }
                    }
                }
            }
        ) { contentPadding ->
            when (state.selectedTab) {
                RequestsTab.MINE -> MyRequestsList(
                    state = state,
                    contentPadding = contentPadding,
                    onRequestClick = onMyRequestClick,
                    onRetry = viewModel::load
                )
                RequestsTab.TO_APPROVE, RequestsTab.HR_INBOX -> PendingList(
                    state = state,
                    isHrInbox = state.selectedTab == RequestsTab.HR_INBOX,
                    contentPadding = contentPadding,
                    onRequestClick = onReviewClick,
                    onApprove = viewModel::approve,
                    onReject = viewModel::showRejectDialog,
                    onResolvedClick = onResolvedClick,
                    onRetry = viewModel::load
                )
                RequestsTab.ALL -> AllRequestsList(
                    state = state,
                    contentPadding = contentPadding,
                    onRequestClick = onReviewClick,
                    onRetry = viewModel::load
                )
            }
        }
    }

    state.rejectForm?.let { form ->
        RejectRequestDialog(
            requesterName = RequestFormatters.shortName(form.request.requesterName),
            reason = form.reason,
            error = form.error,
            isSaving = form.isSaving,
            onReasonChange = viewModel::onRejectReasonChange,
            onConfirm = viewModel::confirmReject,
            onDismiss = viewModel::dismissRejectDialog
        )
    }
}


/**
 * List of the viewer's own requests, with its loading, error and empty states.
 *
 * @param state screen state.
 * @param contentPadding padding that leaves room for the collapsing header.
 * @param onRequestClick called with the identifier of the tapped request.
 * @param onRetry called when "Reintentar" is tapped.
 */
@Composable
private fun MyRequestsList(
    state: RequestsUiState,
    contentPadding: PaddingValues,
    onRequestClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    when {
        state.isLoadingMine -> RequestLoading(modifier = stateModifier)
        state.mineError != null && state.myRequests.isEmpty() -> RequestMessageState(
            title = "No se pudieron cargar tus solicitudes",
            message = state.mineError,
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = stateModifier
        )
        state.visibleMyRequests.isEmpty() -> RequestMessageState(
            title = if (state.myRequests.isEmpty()) "Aún no tienes solicitudes" else "No hay solicitudes con este estado",
            message = if (state.myRequests.isEmpty()) {
                "Pide vacaciones, permisos o licencias con el botón \"Nueva solicitud\"."
            } else {
                "Prueba con otro filtro."
            },
            modifier = stateModifier
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding(contentPadding, bottom = Spacing.fabClearance),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            items(state.visibleMyRequests, key = { it.id }) { request ->
                MyRequestCard(request = request, onClick = { onRequestClick(request.id) })
            }
        }
    }
}


/**
 * List of the requests waiting for the viewer's decision (as boss or as HR), with its states.
 *
 * @param state screen state.
 * @param isHrInbox true for the HR inbox, false for the boss's approvals.
 * @param contentPadding padding that leaves room for the collapsing header.
 * @param onRequestClick called with the identifier of the tapped request.
 * @param onApprove called when a request is approved from its card.
 * @param onReject called when a request is rejected from its card.
 * @param onResolvedClick called when "Ver solicitudes resueltas" is tapped.
 * @param onRetry called when "Reintentar" is tapped.
 */
@Composable
private fun PendingList(
    state: RequestsUiState,
    isHrInbox: Boolean,
    contentPadding: PaddingValues,
    onRequestClick: (Long) -> Unit,
    onApprove: (Request) -> Unit,
    onReject: (Request) -> Unit,
    onResolvedClick: () -> Unit,
    onRetry: () -> Unit
) {
    val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    when {
        state.isLoadingPending -> RequestLoading(modifier = stateModifier)
        state.pendingError != null && state.pending.isEmpty() -> RequestMessageState(
            title = "No se pudo cargar la bandeja",
            message = state.pendingError,
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = stateModifier
        )
        state.pending.isEmpty() -> RequestMessageState(
            title = "No tienes solicitudes por atender",
            message = if (isHrInbox) {
                "Cuando un colaborador sin jefe directo envíe una solicitud, aparecerá aquí."
            } else {
                "Todo lo que llegó está resuelto. Cuando alguien de tu equipo envíe una solicitud, aparecerá aquí y recibirás una notificación."
            },
            icon = FlowboardIcons.Check,
            actionLabel = if (isHrInbox) null else "Ver solicitudes resueltas",
            onAction = onResolvedClick,
            modifier = stateModifier
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding(contentPadding, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            items(state.visiblePending, key = { it.id }) { request ->
                ApprovalCard(
                    request = request,
                    isProcessing = state.processingRequestId == request.id,
                    onClick = { onRequestClick(request.id) },
                    onReject = { onReject(request) },
                    onApprove = { onApprove(request) }
                )
            }
        }
    }
}


/**
 * List of the requests of every employee, with its loading, error and empty states.
 *
 * @param state screen state.
 * @param contentPadding padding that leaves room for the collapsing header.
 * @param onRequestClick called with the identifier of the tapped request.
 * @param onRetry called when "Reintentar" is tapped.
 */
@Composable
private fun AllRequestsList(
    state: RequestsUiState,
    contentPadding: PaddingValues,
    onRequestClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
    when {
        state.isLoadingAll -> RequestLoading(modifier = stateModifier)
        state.allError != null && state.allRequests.isEmpty() -> RequestMessageState(
            title = "No se pudieron cargar las solicitudes",
            message = state.allError,
            actionLabel = "Reintentar",
            onAction = onRetry,
            modifier = stateModifier
        )
        state.visibleAll.isEmpty() -> RequestMessageState(
            title = "No hay solicitudes con este estado",
            message = "Prueba con otro filtro.",
            modifier = stateModifier
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = listPadding(contentPadding, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            items(state.visibleAll, key = { it.id }) { request ->
                RequestListRow(request = request, onClick = { onRequestClick(request.id) })
            }
        }
    }
}


/**
 * Content padding of a list placed below the collapsing header.
 *
 * @param headerPadding padding given by the collapsing header layout.
 * @param bottom space left after the last item.
 * @return padding with the screen margins, the header height and the bottom space.
 */
private fun listPadding(headerPadding: PaddingValues, bottom: Dp): PaddingValues =
    PaddingValues(
        top = headerPadding.calculateTopPadding() + 8.dp,
        bottom = bottom,
        start = Spacing.screenHorizontal,
        end = Spacing.screenHorizontal
    )


/**
 * Single-line row of filter chips that scrolls sideways when the chips do not fit.
 *
 * @param content chips of the row.
 */
@Composable
private fun ChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.screenHorizontal),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        content()
    }
}


/** Filter chip with a check icon while selected. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectableChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        }
    )
}


/** Filter chip that opens a menu to pick one of [options]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownChip(
    label: String,
    selected: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FilterChip(
            selected = selected,
            onClick = { expanded = true },
            label = { Text(label) },
            leadingIcon = if (selected) {
                { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else {
                null
            },
            trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
