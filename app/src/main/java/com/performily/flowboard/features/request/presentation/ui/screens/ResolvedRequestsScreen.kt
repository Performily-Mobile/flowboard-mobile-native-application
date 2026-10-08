package com.performily.flowboard.features.request.presentation.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.request.presentation.ui.components.RequestListRow
import com.performily.flowboard.features.request.presentation.ui.components.RequestLoading
import com.performily.flowboard.features.request.presentation.ui.components.RequestMessageState
import com.performily.flowboard.features.request.presentation.viewmodel.ResolvedRequestsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolvedRequestsScreen(
    onBack: () -> Unit,
    onRequestClick: (requestId: Long) -> Unit,
    viewModel: ResolvedRequestsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitudes resueltas") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> RequestLoading(modifier = contentModifier)
            state.errorMessage != null && state.requests.isEmpty() -> RequestMessageState(
                title = "No se pudieron cargar las solicitudes",
                message = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::load,
                modifier = contentModifier
            )
            state.requests.isEmpty() -> RequestMessageState(
                title = "Aún no hay solicitudes resueltas",
                message = "Las solicitudes de tu equipo que apruebes o rechaces aparecerán aquí.",
                modifier = contentModifier
            )
            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                items(state.requests, key = { it.id }) { request ->
                    RequestListRow(request = request, onClick = { onRequestClick(request.id) })
                }
            }
        }
    }
}
