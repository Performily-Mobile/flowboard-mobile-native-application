package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
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
                    onClick = onNewRequest,
                    icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                    text = { Text("Nueva solicitud") },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            RequestSegmented(
                options = state.tabs,
                selected = state.selectedTab,
                optionLabel = state::tabLabel,
                onSelect = viewModel::onTabSelected,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            when (state.selectedTab) {
                RequestsTab.MINE -> MyRequestsTab(
                    state = state,
                    onFilterSelected = viewModel::onMyFilterSelected,
                    onRequestClick = onMyRequestClick,
                    onRetry = viewModel::load
                )
                RequestsTab.TO_APPROVE, RequestsTab.HR_INBOX -> PendingTab(
                    state = state,
                    isHrInbox = state.selectedTab == RequestsTab.HR_INBOX,
                    onTypeSelected = viewModel::onTypeFilterSelected,
                    onSortSelected = viewModel::onSortSelected,
                    onRequestClick = onReviewClick,
                    onApprove = viewModel::approve,
                    onReject = viewModel::showRejectDialog,
                    onResolvedClick = onResolvedClick,
                    onRetry = viewModel::load
                )
                RequestsTab.ALL -> AllRequestsTab(
                    state = state,
                    onFilterSelected = viewModel::onAllFilterSelected,
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


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun MyRequestsTab(
    state: RequestsUiState,
    onFilterSelected: (MyRequestsFilter) -> Unit,
    onRequestClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MyRequestsFilter.entries.forEach { filter ->
                SelectableChip(
                    label = filter.label,
                    selected = state.myFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
        when {
            state.isLoadingMine -> RequestLoading()
            state.mineError != null && state.myRequests.isEmpty() -> RequestMessageState(
                title = "No se pudieron cargar tus solicitudes",
                message = state.mineError,
                actionLabel = "Reintentar",
                onAction = onRetry
            )
            state.visibleMyRequests.isEmpty() -> RequestMessageState(
                title = if (state.myRequests.isEmpty()) "Aún no tienes solicitudes" else "No hay solicitudes con este estado",
                message = if (state.myRequests.isEmpty()) {
                    "Pide vacaciones, permisos o licencias con el botón \"Nueva solicitud\"."
                } else {
                    "Prueba con otro filtro."
                }
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.visibleMyRequests, key = { it.id }) { request ->
                    MyRequestCard(request = request, onClick = { onRequestClick(request.id) })
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PendingTab(
    state: RequestsUiState,
    isHrInbox: Boolean,
    onTypeSelected: (RequestType?) -> Unit,
    onSortSelected: (ApprovalSort) -> Unit,
    onRequestClick: (Long) -> Unit,
    onApprove: (Request) -> Unit,
    onReject: (Request) -> Unit,
    onResolvedClick: () -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isHrInbox) {
            RequestBanner(
                message = "Aquí llegan las solicitudes de colaboradores que no tienen jefe directo asignado.",
                icon = FlowboardIcons.Info,
                containerColor = RequestColors.InfoBanner,
                contentColor = RequestColors.OnInfoBanner,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        } else {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DropdownChip(
                    label = "Tipo: ${state.typeFilter?.name ?: "todos"}",
                    selected = state.typeFilter != null,
                    options = listOf<RequestType?>(null) + state.requestTypes,
                    optionLabel = { it?.name ?: "Todos" },
                    onSelect = onTypeSelected
                )
                DropdownChip(
                    label = state.sort.label,
                    selected = state.pending.isNotEmpty(),
                    options = ApprovalSort.entries,
                    optionLabel = { it.label },
                    onSelect = onSortSelected
                )
            }
        }
        when {
            state.isLoadingPending -> RequestLoading()
            state.pendingError != null && state.pending.isEmpty() -> RequestMessageState(
                title = "No se pudo cargar la bandeja",
                message = state.pendingError,
                actionLabel = "Reintentar",
                onAction = onRetry
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
                onAction = onResolvedClick
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AllRequestsTab(
    state: RequestsUiState,
    onFilterSelected: (AllRequestsFilter) -> Unit,
    onRequestClick: (Long) -> Unit,
    onRetry: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AllRequestsFilter.entries.forEach { filter ->
                SelectableChip(
                    label = filter.label,
                    selected = state.allFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
        when {
            state.isLoadingAll -> RequestLoading()
            state.allError != null && state.allRequests.isEmpty() -> RequestMessageState(
                title = "No se pudieron cargar las solicitudes",
                message = state.allError,
                actionLabel = "Reintentar",
                onAction = onRetry
            )
            state.visibleAll.isEmpty() -> RequestMessageState(
                title = "No hay solicitudes con este estado",
                message = "Prueba con otro filtro."
            )
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)
            ) {
                items(state.visibleAll, key = { it.id }) { request ->
                    RequestListRow(request = request, onClick = { onRequestClick(request.id) })
                }
            }
        }
    }
}


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
