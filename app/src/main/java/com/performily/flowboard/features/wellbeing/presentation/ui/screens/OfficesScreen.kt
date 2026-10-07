package com.performily.flowboard.features.wellbeing.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.wellbeing.presentation.ui.components.BannerTone
import com.performily.flowboard.features.wellbeing.presentation.ui.components.OfficeCard
import com.performily.flowboard.features.wellbeing.presentation.ui.components.StatusBanner
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingLoading
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingMessageState
import com.performily.flowboard.features.wellbeing.presentation.viewmodel.OfficesViewModel
import kotlinx.coroutines.delay

/** How often the indicators are refreshed while the screen is visible. */
internal const val WELLBEING_REFRESH_MILLIS = 30_000L

/**
 * MA-70 - Offices: overall state of each workspace.
 *
 * The list is reloaded every [WELLBEING_REFRESH_MILLIS] only while the screen is at least
 * started, so nothing is requested while the app is in the background.
 *
 * @param onBack called when the user leaves the screen
 * @param onOfficeClick called with the id of the tapped office
 * @param onNewOfficeClick called when the user wants to register an office
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficesScreen(
    onBack: () -> Unit,
    onOfficeClick: (Long) -> Unit,
    onNewOfficeClick: () -> Unit,
    viewModel: OfficesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                viewModel.load()
                delay(WELLBEING_REFRESH_MILLIS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Espacios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewOfficeClick,
                icon = { Icon(FlowboardIcons.Add, contentDescription = null) },
                text = { Text("Nuevo espacio") },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> WellbeingLoading(modifier = contentModifier)

            state.errorMessage != null && state.offices.isEmpty() -> WellbeingMessageState(
                title = "No se pudieron cargar los espacios",
                message = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::retry,
                modifier = contentModifier
            )

            state.offices.isEmpty() -> WellbeingMessageState(
                title = "Aún no hay espacios",
                message = "Registra un espacio de trabajo para empezar a monitorear su temperatura, iluminación y calidad del aire.",
                actionLabel = "Nuevo espacio",
                onAction = onNewOfficeClick,
                modifier = contentModifier
            )

            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.errorMessage != null) {
                    item {
                        StatusBanner(
                            message = "No se pudo actualizar. Mostrando el último dato disponible.",
                            tone = BannerTone.WARNING
                        )
                    }
                }
                item {
                    Text(
                        text = "Estado ambiental de cada espacio de trabajo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(state.offices, key = { it.office.id }) { status ->
                    OfficeCard(status = status, onClick = { onOfficeClick(status.office.id) })
                }
            }
        }
    }
}
