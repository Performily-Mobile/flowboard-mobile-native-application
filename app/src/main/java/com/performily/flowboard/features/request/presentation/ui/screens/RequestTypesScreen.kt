package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import com.performily.flowboard.features.request.presentation.ui.components.DeleteRequestTypeDialog
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.ui.components.RequestTypeRow
import com.performily.flowboard.features.request.presentation.viewmodel.RequestTypesViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTypesScreen(
    onBack: () -> Unit,
    onNewType: () -> Unit,
    resultMessage: String?,
    onResultMessageConsumed: () -> Unit,
    viewModel: RequestTypesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.load() }

    LaunchedEffect(resultMessage) {
        resultMessage?.let {
            viewModel.onCreated(it)
            onResultMessageConsumed()
        }
    }

    LaunchedEffect(state.snackbar) {
        val snackbar = state.snackbar ?: return@LaunchedEffect
        val typeToDeactivate = snackbar.typeToDeactivate
        val result = snackbarHostState.showSnackbar(
            message = snackbar.message,
            actionLabel = if (typeToDeactivate != null) "Desactivar" else null,
            duration = if (typeToDeactivate != null) SnackbarDuration.Long else SnackbarDuration.Short
        )
        viewModel.onSnackbarShown()
        if (result == SnackbarResult.ActionPerformed && typeToDeactivate != null) {
            viewModel.toggleStatus(typeToDeactivate)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tipos de solicitud") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewType,
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                text = { Text("Nuevo tipo") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> RequestLoading(modifier = contentModifier)
            state.errorMessage != null && state.types.isEmpty() -> RequestMessageState(
                title = "No se pudieron cargar los tipos",
                message = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::load,
                modifier = contentModifier
            )
            state.types.isEmpty() -> RequestMessageState(
                title = "Aún no hay tipos de solicitud",
                message = "Crea el primero con el botón \"Nuevo tipo\".",
                modifier = contentModifier
            )
            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
            ) {
                items(state.types, key = { it.id }) { type ->
                    RequestTypeRow(
                        type = type,
                        onToggleStatus = { viewModel.toggleStatus(type) },
                        onDelete = { viewModel.requestDelete(type) }
                    )
                }
            }
        }
    }

    state.typeToDelete?.let { type ->
        DeleteRequestTypeDialog(
            typeName = type.name,
            isDeleting = state.isWorking,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::dismissDelete
        )
    }
}
