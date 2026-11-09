package com.performily.flowboard.features.dashboard.presentation.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.core.designsystem.theme.Error
import com.performily.flowboard.core.designsystem.theme.Primary
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.dashboard.domain.entity.BenefitsOverview
import com.performily.flowboard.features.dashboard.domain.entity.EmployeeDashboard
import com.performily.flowboard.features.dashboard.domain.entity.MonthAttendance
import com.performily.flowboard.features.dashboard.domain.entity.RequestsOverview
import com.performily.flowboard.features.dashboard.domain.entity.VacationOverview
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardAccent
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardAlertBanner
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardColors
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardErrorBanner
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardFormatters
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardIcons
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardMetricCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardSectionCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.DashboardSectionMessage
import com.performily.flowboard.features.dashboard.presentation.ui.components.EmployeeDashboardFormatters
import com.performily.flowboard.features.dashboard.presentation.ui.components.EmployeeTodayCard
import com.performily.flowboard.features.dashboard.presentation.ui.components.MonthStatRow
import com.performily.flowboard.features.dashboard.presentation.ui.components.MyRequestItem
import com.performily.flowboard.features.dashboard.presentation.viewmodel.EmployeeDashboardViewModel
import java.time.LocalDateTime
import kotlinx.coroutines.delay

/**
 * Home screen of the employee (MA-14): a summary of their own attendance, vacation, requests and
 * benefits.
 *
 * @param onAttendanceClick called when the "Hoy" card, the punctuality card or the month section is tapped.
 * @param onRequestsClick called when the requests card, the banner or "Ver todas" is tapped.
 * @param onRequestClick called with the request identifier when a request is tapped.
 * @param onVacationClick called when the vacation card or section is tapped.
 * @param onBenefitsClick called when the benefits card is tapped.
 * @param onNotificationsClick called when the bell is tapped.
 * @param viewModel employee dashboard view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeDashboardScreen(
    onAttendanceClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onRequestClick: (Long) -> Unit,
    onVacationClick: () -> Unit,
    onBenefitsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    viewModel: EmployeeDashboardViewModel = hiltViewModel()
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
                title = "Inicio",
                subtitle = DashboardFormatters.subtitle(dashboard?.firstName, dashboard?.loadedAt, now),
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        BadgedBox(
                            badge = {
                                if ((dashboard?.requests?.underReview ?: 0) > 0) Badge()
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
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
                    val underReview = dashboard?.requests?.underReview ?: 0
                    if (underReview > 0) {
                        item {
                            DashboardAlertBanner(
                                text = if (underReview == 1) {
                                    "Tienes 1 solicitud devuelta para revisión"
                                } else {
                                    "Tienes $underReview solicitudes devueltas para revisión"
                                },
                                onClick = onRequestsClick
                            )
                        }
                    }
                    item { EmployeeTodayCard(today = dashboard?.today, onClick = onAttendanceClick) }
                    item {
                        EmployeeMetricsGrid(
                            dashboard = dashboard,
                            onAttendanceClick = onAttendanceClick,
                            onVacationClick = onVacationClick,
                            onRequestsClick = onRequestsClick,
                            onBenefitsClick = onBenefitsClick
                        )
                    }
                    item { MonthSection(month = dashboard?.month, onSeeAll = onAttendanceClick) }
                    item { VacationSection(vacation = dashboard?.vacation, onSeeAll = onVacationClick) }
                    item {
                        RequestsSection(
                            requests = dashboard?.requests,
                            onSeeAll = onRequestsClick,
                            onRequestClick = onRequestClick
                        )
                    }
                }
            }
        }
    }
}

/** Two rows of two indicator cards: punctuality, vacation, requests and benefits. */
@Composable
private fun EmployeeMetricsGrid(
    dashboard: EmployeeDashboard?,
    onAttendanceClick: () -> Unit,
    onVacationClick: () -> Unit,
    onRequestsClick: () -> Unit,
    onBenefitsClick: () -> Unit
) {
    val month = dashboard?.month
    val vacation = dashboard?.vacation
    val requests = dashboard?.requests
    val benefits = dashboard?.benefits
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            DashboardMetricCard(
                label = "Puntualidad del mes",
                value = EmployeeDashboardFormatters.percentage(month?.punctualityPercentage),
                icon = FlowboardIcons.Schedule,
                accent = DashboardAccent.GREEN,
                supportingText = punctualitySupport(month),
                onClick = onAttendanceClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DashboardMetricCard(
                label = "Vacaciones disponibles",
                value = EmployeeDashboardFormatters.days(vacation?.availableDays),
                icon = FlowboardIcons.Calendar,
                accent = DashboardAccent.PRIMARY,
                supportingText = vacation?.let { "de ${EmployeeDashboardFormatters.days(it.accruedDays)} días acumulados" },
                onClick = onVacationClick,
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
            DashboardMetricCard(
                label = "Solicitudes pendientes",
                value = DashboardFormatters.count(requests?.pending),
                icon = FlowboardIcons.Article,
                accent = DashboardAccent.AMBER,
                supportingText = requests?.let { requestsSupport(it) },
                onClick = onRequestsClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            DashboardMetricCard(
                label = "Beneficios vigentes",
                value = DashboardFormatters.count(benefits?.currentCount),
                icon = FlowboardIcons.Gift,
                accent = DashboardAccent.PURPLE,
                supportingText = benefits?.let { benefitsSupport(it) },
                onClick = onBenefitsClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/** Returns the note of the punctuality card, for example "2 tardanzas este mes". */
private fun punctualitySupport(month: MonthAttendance?): String? = when {
    month == null -> null
    month.checkIns == 0 -> "Sin marcaciones este mes"
    month.late == 0 -> "Sin tardanzas este mes"
    month.late == 1 -> "1 tardanza este mes"
    else -> "${month.late} tardanzas este mes"
}

/** Returns the note of the requests card: how many were returned for review, or that all is up to date. */
private fun requestsSupport(requests: RequestsOverview): String = when {
    requests.underReview == 1 -> "1 devuelta para revisión"
    requests.underReview > 1 -> "${requests.underReview} devueltas para revisión"
    requests.pending == 0 -> "Todo al día"
    else -> "esperando respuesta"
}

/** Returns the note of the benefits card: the nearest expiry, or how many were delivered. */
private fun benefitsSupport(benefits: BenefitsOverview): String =
    benefits.nextToExpire?.let { "${it.name} · ${EmployeeDashboardFormatters.expiry(it.endDate)}" }
        ?: "${benefits.deliveredCount} entregados"

/** Card with the days of the month by status and the hours worked, or a message when it cannot be shown. */
@Composable
private fun MonthSection(
    month: MonthAttendance?,
    onSeeAll: () -> Unit
) {
    DashboardSectionCard(title = "Mi asistencia del mes", action = "Ver", onAction = onSeeAll) {
        when {
            month == null -> DashboardSectionMessage("No se pudo cargar tu asistencia del mes.")
            month.totalDays == 0 -> DashboardSectionMessage("Todavía no tienes marcaciones este mes.")
            else -> {
                MonthStatRow("Puntuales", month.onTime, month.totalDays, DashboardColors.healthy)
                MonthStatRow("Tardanzas", month.late, month.totalDays, DashboardColors.warning)
                MonthStatRow("Inasistencias", month.absent, month.totalDays, Error)
                MonthStatRow("Justificadas", month.justified, month.totalDays, Primary)
                HorizontalDivider(color = Divider, modifier = Modifier.padding(vertical = Spacing.s))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Horas trabajadas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = EmployeeDashboardFormatters.hours(month.workedHours),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (month.overtimeHours > 0.0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Sobretiempo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = EmployeeDashboardFormatters.hours(month.overtimeHours),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

/** Card with the vacation balance and how much of it was already taken. */
@Composable
private fun VacationSection(
    vacation: VacationOverview?,
    onSeeAll: () -> Unit
) {
    DashboardSectionCard(title = "Mis vacaciones", action = "Ver", onAction = onSeeAll) {
        if (vacation == null) {
            DashboardSectionMessage("No se pudo cargar tu saldo de vacaciones.")
        } else {
            Column(
                modifier = Modifier.padding(vertical = Spacing.s),
                verticalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Text(
                    text = "${EmployeeDashboardFormatters.days(vacation.availableDays)} días disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LinearProgressIndicator(
                    progress = { vacation.usedRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Text(
                    text = "Gozaste ${EmployeeDashboardFormatters.days(vacation.usedDays)} de " +
                        "${EmployeeDashboardFormatters.days(vacation.accruedDays)} días acumulados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (vacation.fromCache) {
                    Text(
                        text = "Sin conexión: datos guardados el ${EmployeeDashboardFormatters.syncedAt(vacation.syncedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/** Card with the latest requests of the employee, or a message when they cannot be shown. */
@Composable
private fun RequestsSection(
    requests: RequestsOverview?,
    onSeeAll: () -> Unit,
    onRequestClick: (Long) -> Unit
) {
    DashboardSectionCard(title = "Mis solicitudes recientes", action = "Ver todas", onAction = onSeeAll) {
        when {
            requests == null -> DashboardSectionMessage("No se pudieron cargar tus solicitudes.")
            requests.recent.isEmpty() -> DashboardSectionMessage("Todavía no has enviado solicitudes.")
            else -> requests.recent.forEachIndexed { index, request ->
                if (index > 0) HorizontalDivider(color = Divider)
                MyRequestItem(request = request, onClick = { onRequestClick(request.id) })
            }
        }
    }
}

private const val MINUTE_MILLIS = 60_000L
