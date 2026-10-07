package com.performily.flowboard.features.wellbeing.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.entity.OfficeStatus
import com.performily.flowboard.features.wellbeing.domain.valueobject.HealthIndicator
import com.performily.flowboard.features.wellbeing.presentation.state.OfficeDetailUiState
import com.performily.flowboard.features.wellbeing.presentation.ui.components.BannerTone
import com.performily.flowboard.features.wellbeing.presentation.ui.components.DeviceRow
import com.performily.flowboard.features.wellbeing.presentation.ui.components.LinkDeviceSheet
import com.performily.flowboard.features.wellbeing.presentation.ui.components.MetricStatusCard
import com.performily.flowboard.features.wellbeing.presentation.ui.components.StatusBanner
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingFormatters
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingLoading
import com.performily.flowboard.features.wellbeing.presentation.ui.components.WellbeingMessageState
import com.performily.flowboard.features.wellbeing.presentation.viewmodel.OfficeDetailViewModel
import kotlinx.coroutines.delay

/**
 * MA-72 · Indicadores de un espacio y MA-81 · Sin lecturas.
 * Muestra el aviso que corresponda, una tarjeta por métrica y los dispositivos vinculados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficeDetailScreen(
    officeId: Long,
    onBack: () -> Unit,
    onThresholdsClick: (officeId: Long, officeName: String) -> Unit,
    onHistoryClick: (officeId: Long, officeName: String) -> Unit,
    viewModel: OfficeDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Se refresca al entrar (también al volver de Umbrales) y cada 30 s.
    LaunchedEffect(officeId) {
        while (true) {
            viewModel.load(officeId)
            delay(WELLBEING_REFRESH_MILLIS)
        }
    }

    LaunchedEffect(state.snackbarMessage) {
        state.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onSnackbarShown()
        }
    }

    val status = state.status

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = status?.office?.name ?: "Indicadores",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
            if (status != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onThresholdsClick(status.office.id, status.office.name) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text("Configurar umbrales") }
                    OutlinedButton(
                        onClick = { onHistoryClick(status.office.id, status.office.name) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) { Text("Ver histórico") }
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
        when {
            state.isLoading -> WellbeingLoading(modifier = contentModifier)

            status == null -> WellbeingMessageState(
                title = "No se pudo cargar el espacio",
                message = state.errorMessage ?: "Inténtalo nuevamente.",
                actionLabel = "Reintentar",
                onAction = { viewModel.load(officeId) },
                modifier = contentModifier
            )

            else -> OfficeDetailContent(
                status = status,
                onLinkClick = viewModel::showLinkSheet,
                onDeviceClick = viewModel::requestUnlink,
                modifier = contentModifier
            )
        }
    }

    if (state.isLinkSheetVisible && status != null) {
        LinkDeviceSheet(
            officeName = status.office.name,
            code = state.linkForm.code,
            codeError = state.linkForm.codeError,
            inventory = state.linkForm.inventory,
            isLoadingInventory = state.linkForm.isLoadingInventory,
            isLinking = state.linkForm.isLinking,
            onCodeChange = viewModel::onLinkCodeChange,
            onLink = viewModel::onLink,
            onDismiss = viewModel::dismissLinkSheet
        )
    }

    state.deviceToUnlink?.let { device ->
        UnlinkDeviceDialog(
            device = device,
            state = state,
            onConfirm = viewModel::confirmUnlink,
            onDismiss = viewModel::dismissUnlink
        )
    }
}

@Composable
private fun OfficeDetailContent(
    status: OfficeStatus,
    onLinkClick: () -> Unit,
    onDeviceClick: (Device) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = listOfNotNull(status.office.area, status.office.location.summary).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        bannerFor(status)?.let { (message, tone) -> StatusBanner(message = message, tone = tone) }

        status.metrics.forEach { metric -> MetricStatusCard(metric = metric) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dispositivos", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = onLinkClick) {
                Icon(FlowboardIcons.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("Vincular", modifier = Modifier.padding(start = 4.dp))
            }
        }

        if (status.devices.isEmpty()) {
            Text(
                text = "No hay dispositivos vinculados. Vincula uno para empezar a recibir lecturas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Column {
                status.devices.forEach { device ->
                    DeviceRow(device = device, onClick = { onDeviceClick(device) })
                }
            }
            Text(
                text = "Toca un dispositivo para desvincularlo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Aviso superior, en este orden de prioridad:
 * 1. Sin ninguna lectura (MA-81).
 * 2. El espacio dejó de reportar: no se calcula el indicador (MA-81).
 * 3. Una métrica dejó de reportar mientras las demás siguen al día.
 * 4. Una métrica vigente en nivel deficiente o peligroso (MA-72).
 */
private fun bannerFor(status: OfficeStatus): Pair<String, BannerTone>? {
    val lastReading = status.lastReadingAt
    if (lastReading == null) {
        val message = if (status.devices.isEmpty()) {
            "Este espacio todavía no tiene lecturas. Vincula un dispositivo de medición para empezar a monitorearlo."
        } else {
            "Este espacio todavía no tiene lecturas. Los dispositivos vinculados aún no han enviado datos."
        }
        return message to BannerTone.WARNING
    }
    if (!status.upToDate) {
        return "No reporta lecturas desde hace ${WellbeingFormatters.elapsed(lastReading)}. " +
            "Mientras no haya datos recientes no se calcula su indicador." to BannerTone.WARNING
    }
    status.metrics.firstOrNull { !it.upToDate && it.lastRecordedAt != null }?.let { stale ->
        return "${WellbeingFormatters.alertName(stale.metricType)} no reporta lecturas desde hace " +
            "${WellbeingFormatters.elapsed(stale.lastRecordedAt!!)}. No se calcula su indicador." to BannerTone.WARNING
    }
    val worst = status.worstMetric ?: return null
    val indicator = worst.indicator ?: return null
    if (!indicator.isWorseThan(HealthIndicator.ACCEPTABLE)) return null
    val time = worst.lastRecordedAt?.let { " (última lectura ${WellbeingFormatters.time(it)})" }.orEmpty()
    return "${WellbeingFormatters.alertName(worst.metricType)} en nivel ${WellbeingFormatters.levelName(indicator)}$time." to
        BannerTone.DANGER
}

@Composable
private fun UnlinkDeviceDialog(
    device: Device,
    state: OfficeDetailUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!state.isUnlinking) onDismiss() },
        title = { Text("¿Desvincular ${device.code}?") },
        text = {
            Text(
                "El dispositivo dejará de enviar lecturas a ${state.status?.office?.name ?: "este espacio"} " +
                    "y volverá al inventario. Las lecturas ya registradas se conservan."
            )
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !state.isUnlinking, shape = RoundedCornerShape(8.dp)) {
                Text("Desvincular")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !state.isUnlinking) { Text("Cancelar") }
        }
    )
}
