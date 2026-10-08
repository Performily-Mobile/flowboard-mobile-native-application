package com.performily.flowboard.features.dashboard.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.dashboard.domain.entity.HrDashboard
import com.performily.flowboard.features.dashboard.presentation.ui.components.AreaAttendanceRow
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardAccent
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardFormatters
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardIcons
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardMetricCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.RequestToAttendItem
import com.performily.flowboard.features.dashboard.presentation.viewmodel.HrDashboardViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.delay

/**
 * HR dashboard screen (MA-18).
 *
 * @param onActiveEmployeesClick called when the active employees card is tapped.
 * @param onPendingRequestsClick called when the pending requests card, the banner or "Ver todas" is tapped.
 * @param onLatenessClick called when the monthly lateness card is tapped.
 * @param onExpiringVacationsClick called when the expiring vacations card is tapped.
 * @param onAttendanceByAreaClick called when "Ver" in the attendance section is tapped.
 * @param onRequestClick called with the request identifier when a request is tapped.
 * @param onNotificationsClick called when the bell is tapped.
 * @param viewModel dashboard view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HrDashboardScreen(
    onActiveEmployeesClick: () -> Unit,
    onPendingRequestsClick: () -> Unit,
    onLatenessClick: () -> Unit,
    onExpiringVacationsClick: () -> Unit,
    onAttendanceByAreaClick: () -> Unit,
    onRequestClick: (Long) -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: HrDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val dashboard = state.dashboard
    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    LaunchedEffect(dashboard?.loadedAt) {
        while (true) {
            now = LocalDateTime.now()
            delay(MINUTE_MILLIS)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Panel", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = DashboardFormatters.subtitle(dashboard?.userFirstName, dashboard?.loadedAt, now),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if ((dashboard?.pendingRequests ?: 0) > 0) Badge()
                            }
                        ) {
                            Icon(DashboardIcons.Notifications, contentDescription = "Notificaciones")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    state.errorMessage?.let { message ->
                        item { ErrorBanner(message = message, onRetry = viewModel::refresh) }
                    }
                    val pending = dashboard?.pendingRequests ?: 0
                    if (pending > 0) {
                        item { PendingBanner(count = pending, onClick = onPendingRequestsClick) }
                    }
                    item {
                        MetricsGrid(
                            dashboard = dashboard,
                            onActiveEmployeesClick = onActiveEmployeesClick,
                            onPendingRequestsClick = onPendingRequestsClick,
                            onLatenessClick = onLatenessClick,
                            onExpiringVacationsClick = onExpiringVacationsClick
                        )
                    }
                    item { AttendanceSection(dashboard, onAttendanceByAreaClick) }
                    item { RequestsSection(dashboard, onPendingRequestsClick, onRequestClick) }
                }
            }
        }
    }
}

/** Banner that calls attention to the requests waiting for HR and opens the inbox. */
@Composable
private fun PendingBanner(count: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = DashboardAccent.AMBER.container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = FlowboardIcons.Article,
                contentDescription = null,
                tint = DashboardAccent.AMBER.content,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = if (count == 1) "Tienes 1 solicitud por atender" else "Tienes $count solicitudes por atender",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = DashboardAccent.AMBER.content,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = FlowboardIcons.ChevronRight,
                contentDescription = null,
                tint = DashboardAccent.AMBER.content
            )
        }
    }
}

/** Two rows of two indicator cards: employees, requests, lateness and expiring vacations. */
@Composable
private fun MetricsGrid(
    dashboard: HrDashboard?,
    onActiveEmployeesClick: () -> Unit,
    onPendingRequestsClick: () -> Unit,
    onLatenessClick: () -> Unit,
    onExpiringVacationsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardMetricCard(
                label = "Colaboradores activos",
                value = DashboardFormatters.count(dashboard?.activeEmployees),
                icon = FlowboardIcons.Group,
                accent = DashboardAccent.PRIMARY,
                onClick = onActiveEmployeesClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DashboardMetricCard(
                label = "Solicitudes pendientes",
                value = DashboardFormatters.count(dashboard?.pendingRequests),
                icon = FlowboardIcons.Article,
                accent = DashboardAccent.AMBER,
                onClick = onPendingRequestsClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val lateness = dashboard?.monthlyLateness
            DashboardMetricCard(
                label = "Tardanzas del mes",
                value = DashboardFormatters.count(lateness?.lateCount),
                icon = FlowboardIcons.Schedule,
                accent = DashboardAccent.RED,
                supportingText = lateness?.let { DashboardFormatters.lateness(it.percentage) },
                onClick = onLatenessClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DashboardMetricCard(
                label = "Vacaciones por vencer",
                value = DashboardFormatters.count(dashboard?.expiringVacations),
                icon = FlowboardIcons.Calendar,
                accent = DashboardAccent.GREEN,
                supportingText = "con 30 días o más sin gozar",
                onClick = onExpiringVacationsClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/** Card with today's attendance per area, or a message when it cannot be shown. */
@Composable
private fun AttendanceSection(
    dashboard: HrDashboard?,
    onSeeAll: () -> Unit
) {
    SectionCard(title = "Asistencia de hoy por área", action = "Ver", onAction = onSeeAll) {
        val attendance = dashboard?.todayAttendance
        when {
            attendance == null -> SectionMessage("No se pudo cargar la asistencia de hoy.")
            attendance.isEmpty() -> SectionMessage("Todavía no hay áreas con colaboradores activos.")
            else -> {
                attendance.forEach { AreaAttendanceRow(it) }
                if (attendance.all { it.percentage == 0 }) {
                    SectionMessage("Nadie ha marcado entrada hoy todavía.")
                }
            }
        }
    }
}

/** Card with the oldest pending requests, or a message when they cannot be shown. */
@Composable
private fun RequestsSection(
    dashboard: HrDashboard?,
    onSeeAll: () -> Unit,
    onRequestClick: (Long) -> Unit
) {
    SectionCard(title = "Solicitudes por atender", action = "Ver todas", onAction = onSeeAll) {
        val requests = dashboard?.requestsToAttend
        when {
            requests == null -> SectionMessage("No se pudieron cargar las solicitudes.")
            requests.isEmpty() -> SectionMessage("No hay solicitudes por atender.")
            else -> requests.forEachIndexed { index, request ->
                if (index > 0) HorizontalDivider(color = Divider)
                RequestToAttendItem(request = request, onClick = { onRequestClick(request.requestId) })
            }
        }
    }
}

/**
 * Rounded card with a title, a text button on the right and free content below.
 *
 * @param title section title.
 * @param action label of the text button.
 * @param onAction called when the text button is tapped.
 * @param content content of the section.
 */
@Composable
private fun SectionCard(
    title: String,
    action: String,
    onAction: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Divider),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onAction) { Text(action) }
            }
            Column(modifier = Modifier.padding(end = 8.dp)) {
                content()
            }
        }
    }
}

/** Short muted message shown in place of a section that has no content. */
@Composable
private fun SectionMessage(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

/** Banner shown when nothing could be loaded, with a retry button. */
@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

private const val MINUTE_MILLIS = 60_000L
