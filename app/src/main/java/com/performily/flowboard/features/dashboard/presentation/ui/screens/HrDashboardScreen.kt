package com.performily.flowboard.features.dashboard.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.dashboard.domain.entity.HrDashboard
import com.performily.flowboard.features.dashboard.presentation.ui.components.AreaAttendanceRow
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardAccent
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardAlertBanner
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardErrorBanner
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardFormatters
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardIcons
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardMetricCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardSectionCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardSectionMessage
import com.performily.flowboard.features.dashboard.presentation.ui.components.RequestToAttendItem
import com.performily.flowboard.features.dashboard.presentation.viewmodel.HrDashboardViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.delay
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.SkeletonDashboard

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
            FlowboardTopBar(
                title = "Panel",
                subtitle = DashboardFormatters.subtitle(dashboard?.userFirstName, dashboard?.loadedAt, now),
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
                }
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
                SkeletonDashboard(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.screenHorizontal,
                        end = Spacing.screenHorizontal,
                        top = Spacing.s,
                        bottom = Spacing.xl
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
                ) {
                    state.errorMessage?.let { message ->
                        item { DashboardErrorBanner(message = message, onRetry = viewModel::refresh) }
                    }
                    val pending = dashboard?.pendingRequests ?: 0
                    if (pending > 0) {
                        item {
                            DashboardAlertBanner(
                                text = if (pending == 1) "Tienes 1 solicitud por atender" else "Tienes $pending solicitudes por atender",
                                onClick = onPendingRequestsClick
                            )
                        }
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

/** Two rows of two indicator cards: employees, requests, lateness and expiring vacations. */
@Composable
private fun MetricsGrid(
    dashboard: HrDashboard?,
    onActiveEmployeesClick: () -> Unit,
    onPendingRequestsClick: () -> Unit,
    onLatenessClick: () -> Unit,
    onExpiringVacationsClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
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
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
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
    DashboardSectionCard(title = "Asistencia de hoy por área", action = "Ver", onAction = onSeeAll) {
        val attendance = dashboard?.todayAttendance
        when {
            attendance == null -> DashboardSectionMessage("No se pudo cargar la asistencia de hoy.")
            attendance.isEmpty() -> DashboardSectionMessage("Todavía no hay áreas con colaboradores activos.")
            else -> {
                attendance.forEach { AreaAttendanceRow(it) }
                if (attendance.all { it.percentage == 0 }) {
                    DashboardSectionMessage("Nadie ha marcado entrada hoy todavía.")
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
    DashboardSectionCard(title = "Solicitudes por atender", action = "Ver todas", onAction = onSeeAll) {
        val requests = dashboard?.requestsToAttend
        when {
            requests == null -> DashboardSectionMessage("No se pudieron cargar las solicitudes.")
            requests.isEmpty() -> DashboardSectionMessage("No hay solicitudes por atender.")
            else -> requests.forEachIndexed { index, request ->
                if (index > 0) HorizontalDivider(color = Divider)
                RequestToAttendItem(request = request, onClick = { onRequestClick(request.requestId) })
            }
        }
    }
}

private const val MINUTE_MILLIS = 60_000L
