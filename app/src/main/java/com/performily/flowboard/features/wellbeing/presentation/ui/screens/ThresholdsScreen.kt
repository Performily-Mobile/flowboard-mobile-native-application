package com.performily.flowboard.features.wellbeing.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType
import com.performily.flowboard.features.wellbeing.presentation.ui.components.BannerTone
import com.performily.flowboard.features.wellbeing.presentation.ui.components.MetricSelector
import com.performily.flowboard.features.wellbeing.presentation.ui.components.OutlinedPanel
import com.performily.flowboard.features.wellbeing.presentation.ui.components.StatusBanner
import com.performily.flowboard.features.wellbeing.presentation.ui.components.ThresholdRangeRow
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingFormatters
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingLoading
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingMessageState
import com.performily.flowboard.features.wellbeing.presentation.viewmodel.ThresholdsViewModel

/**
 * MA-74 - Thresholds: ranges of each indicator per metric.
 *
 * @param officeId office being configured
 * @param officeName name shown in the top bar
 * @param onBack called when the user leaves the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThresholdsScreen(
    officeId: Long,
    officeName: String,
    onBack: () -> Unit,
    viewModel: ThresholdsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(officeId) {
        viewModel.load(officeId, officeName)
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
                title = {
                    Column {
                        Text("Umbrales")
                        Text(
                            text = officeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (!state.isLoading && state.errorMessage == null) {
                Button(
                    onClick = viewModel::onSave,
                    enabled = state.canSave,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp)
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar umbrales")
                    }
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> WellbeingLoading(modifier = contentModifier)

            state.errorMessage != null -> WellbeingMessageState(
                title = "No se pudieron cargar los umbrales",
                message = state.errorMessage.orEmpty(),
                actionLabel = "Reintentar",
                onAction = viewModel::retry,
                modifier = contentModifier
            )

            else -> Column(
                modifier = contentModifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricSelector(selected = state.selectedMetric, onSelect = viewModel::onMetricSelected)

                state.generalError?.let { StatusBanner(message = it, tone = BannerTone.DANGER) }
                state.saveError?.let { StatusBanner(message = it, tone = BannerTone.DANGER) }

                OutlinedPanel {
                    Text(WellbeingFormatters.thresholdTitle(state.selectedMetric), style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = if (state.isConfigured) {
                            "Rangos guardados. El mínimo se incluye y el máximo no: cada nivel empieza donde termina el anterior."
                        } else {
                            "Valores sugeridos: esta métrica aún no tiene umbrales. Ajústalos y guarda."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    state.rows.forEach { row ->
                        ThresholdRangeRow(
                            indicator = row.indicator,
                            min = row.min,
                            max = row.max,
                            error = state.rowErrors[row.indicator],
                            onMinChange = { viewModel.onMinChange(row.indicator, it) },
                            onMaxChange = { viewModel.onMaxChange(row.indicator, it) }
                        )
                    }
                }

                Text(
                    text = "Deja vacía una fila si no quieres usar ese nivel. Define al menos 2 niveles, " +
                        "sin superposiciones ni vacíos entre ellos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.selectedMetric == MetricType.TEMPERATURE) {
                    Text(
                        text = "Las lecturas por debajo del primer rango se clasifican con el nivel de ese primer rango.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
