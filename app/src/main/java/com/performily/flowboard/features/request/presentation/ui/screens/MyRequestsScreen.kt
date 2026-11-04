package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.request.presentation.state.MyRequestsFilter
import com.performily.flowboard.features.request.presentation.ui.components.MyRequestCard
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoadMoreItem
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.viewmodel.MyRequestsViewModel

/**
 * Own requests of an HR staff member, opened from "Más". HR staff are also employees, so they can
 * see their requests, open them and send new ones.
 *
 * @param onBack called when the back button is tapped.
 * @param onNewRequest called when "Nueva solicitud" is tapped.
 * @param onRequestClick called with the identifier of the tapped request.
 * @param resultMessage message left by a previous screen, shown in a snackbar.
 * @param onResultMessageConsumed called once [resultMessage] has been handled.
 * @param viewModel my requests view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRequestsScreen(
    onBack: () -> Unit,
    onNewRequest: () -> Unit,
    onRequestClick: (requestId: Long) -> Unit,
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    viewModel: MyRequestsViewModel = hiltViewModel()
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
            FlowboardTopBar(
                title = "Mis solicitudes",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nueva solicitud") },
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                onClick = onNewRequest,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.screenHorizontal),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                MyRequestsFilter.entries.forEach { filter ->
                    val selected = state.filter == filter
                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.onFilterSelected(filter) },
                        label = { Text(filter.label) },
                        leadingIcon = if (selected) {
                            { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else {
                            null
                        }
                    )
                }
            }
            when {
                state.isLoading -> RequestLoading()
                state.errorMessage != null && state.requests.isEmpty() -> RequestMessageState(
                    title = "No se pudieron cargar tus solicitudes",
                    message = state.errorMessage.orEmpty(),
                    actionLabel = "Reintentar",
                    onAction = viewModel::load
                )
                state.requests.isEmpty() -> RequestMessageState(
                    title = if (state.filter == MyRequestsFilter.ALL) "Aún no tienes solicitudes" else "No hay solicitudes con este estado",
                    message = if (state.filter == MyRequestsFilter.ALL) {
                        "Pide vacaciones, permisos o licencias con el botón \"Nueva solicitud\"."
                    } else {
                        "Prueba con otro filtro."
                    }
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal,
                        top = Spacing.xs,
                        bottom = Spacing.fabClearance
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
                ) {
                    items(state.requests, key = { it.id }) { request ->
                        MyRequestCard(request = request, onClick = { onRequestClick(request.id) })
                    }
                    if (state.paging.hasMore) {
                        item(key = "load-more") {
                            RequestLoadMoreItem(
                                itemCount = state.requests.size,
                                failed = state.paging.loadMoreFailed,
                                onLoadMore = viewModel::loadMore
                            )
                        }
                    }
                }
            }
        }
    }
}
