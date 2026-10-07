package com.performily.flowboard.features.wellbeing.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.wellbeing.domain.entity.ReadingHistory
import com.performily.flowboard.features.wellbeing.presentation.state.summary
import com.performily.flowboard.features.wellbeing.presentation.ui.components.BannerTone
import com.performily.flowboard.features.wellbeing.presentation.ui.components.DailyAverageChart
import com.performily.flowboard.features.wellbeing.presentation.ui.components.DateRangeField
import com.performily.flowboard.features.wellbeing.presentation.ui.components.MetricSelector
import com.performily.flowboard.features.wellbeing.presentation.ui.components.StatCards
import com.performily.flowboard.features.wellbeing.presentation.ui.components.StatusBanner
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingMessageState
import com.performily.flowboard.features.wellbeing.presentation.viewmodel.ReadingHistoryViewModel

/** MA-75 · Histórico: promedio diario, máximo, mínimo y promedio del período. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingHistoryScreen(
    officeId: Long,
    officeName: String,
    onBack: () -> Unit,
    viewModel: ReadingHistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(officeId) {
        viewModel.load(officeId, officeName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Histórico")
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
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricSelector(selected = state.selectedMetric, onSelect = viewModel::onMetricSelected)
            DateRangeField(from = state.from, to = state.to, onRangeChange = viewModel::onRangeChange)

            val history = state.history
            when {
                state.isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                state.errorMessage != null -> WellbeingMessageState(
                    title = "No se pudo cargar el histórico",
                    message = state.errorMessage.orEmpty(),
                    actionLabel = "Reintentar",
                    onAction = viewModel::retry
                )

                history == null -> Unit

                history.isEmpty -> WellbeingMessageState(
                    title = "Sin lecturas en este período",
                    message = history.message
                        ?: "No hay lecturas registradas entre estas fechas. Prueba con otro rango o revisa que el espacio tenga dispositivos vinculados."
                )

                else -> HistoryContent(history = history)
            }
        }
    }
}

@Composable
private fun HistoryContent(history: ReadingHistory) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (history.isPersistentProblem) {
            StatusBanner(message = history.summary(), tone = BannerTone.DANGER)
        }
        DailyAverageChart(metricType = history.metricType, dailyAverages = history.dailyAverages)
        StatCards(
            metricType = history.metricType,
            maximum = history.maximum,
            minimum = history.minimum,
            average = history.average
        )
        if (!history.isPersistentProblem) {
            Text(
                text = history.summary(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "${history.readingsCount} lecturas en el período.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
