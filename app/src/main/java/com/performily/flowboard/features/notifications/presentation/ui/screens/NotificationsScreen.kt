package com.performily.flowboard.features.notifications.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.component.CollapsingHeaderLayout
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.component.rememberCollapsingHeaderState
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.notifications.domain.entity.AppNotification
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationTarget
import com.performily.flowboard.features.notifications.presentation.state.NotificationFilter
import com.performily.flowboard.features.notifications.presentation.ui.components.NotificationFormatters
import com.performily.flowboard.features.notifications.presentation.ui.components.NotificationItem
import com.performily.flowboard.features.notifications.presentation.viewmodel.NotificationsViewModel
import java.time.LocalDateTime
import com.performily.flowboard.core.designsystem.component.SkeletonList

/**
 * Notices of the signed-in user, grouped by day: changes in their requests, requests to approve,
 * published or paid payslips and benefits. Tapping a notice marks it as read and opens its screen.
 *
 * @param onBack called when the back button is tapped.
 * @param onOpenTarget called with the screen a tapped notice has to open.
 * @param viewModel notifications view model.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenTarget: (NotificationTarget) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val headerState = rememberCollapsingHeaderState()
    val now = remember(state.notifications) { LocalDateTime.now() }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Notificaciones",
                onBack = onBack,
                actions = {
                    if (state.unreadCount > 0) {
                        TextButton(onClick = viewModel::markAllAsRead) { Text("Marcar leídas") }
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
            CollapsingHeaderLayout(
                state = headerState,
                showHeaderKey = listOf(
                    state.filter,
                    state.visibleNotifications.size,
                    state.isLoading,
                    state.errorMessage != null
                ),
                header = {
                    FlowboardTabs(
                        options = NotificationFilter.entries,
                        selected = state.filter,
                        onSelect = viewModel::selectFilter,
                        label = { filter ->
                            if (filter == NotificationFilter.UNREAD && state.unreadCount > 0) {
                                "${filter.label} (${state.unreadCount})"
                            } else {
                                filter.label
                            }
                        },
                        modifier = Modifier.padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.s)
                    )
                }
            ) { contentPadding ->
                val stateModifier = Modifier.padding(top = contentPadding.calculateTopPadding())
                val visible = state.visibleNotifications
                when {
                    state.isLoading -> SkeletonList(modifier = stateModifier.fillMaxSize(), showTrailing = false)
                    state.errorMessage != null && state.notifications.isEmpty() -> MessageState(
                        title = "No se pudieron cargar tus notificaciones",
                        message = state.errorMessage.orEmpty(),
                        actionLabel = "Reintentar",
                        onAction = viewModel::refresh,
                        modifier = stateModifier
                    )
                    visible.isEmpty() -> MessageState(
                        title = if (state.filter == NotificationFilter.UNREAD) "Estás al día" else "Sin notificaciones",
                        message = if (state.filter == NotificationFilter.UNREAD) {
                            "No tienes notificaciones sin leer."
                        } else {
                            "Aquí verás los cambios de tus solicitudes, tus boletas y tus beneficios."
                        },
                        modifier = stateModifier
                    )
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = Spacing.screenHorizontal,
                            end = Spacing.screenHorizontal,
                            top = contentPadding.calculateTopPadding() + Spacing.xs,
                            bottom = Spacing.xl
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.s)
                    ) {
                        if (state.hasMissingSources) {
                            item(key = "partial") { PartialBanner() }
                        }
                        groupByDay(visible, now).forEach { (group, notifications) ->
                            item(key = "group-$group") {
                                Text(
                                    text = group,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(
                                        start = Spacing.xs,
                                        top = Spacing.m,
                                        bottom = Spacing.xs
                                    )
                                )
                            }
                            items(notifications, key = { it.id }) { notification ->
                                NotificationItem(
                                    notification = notification,
                                    time = NotificationFormatters.relativeTime(notification.occurredAt, now),
                                    onClick = {
                                        viewModel.onOpened(notification)
                                        onOpenTarget(notification.target)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Splits the notices into day groups, keeping their order.
 *
 * @param notifications notices, the most recent first.
 * @param now current moment.
 */
private fun groupByDay(
    notifications: List<AppNotification>,
    now: LocalDateTime
): List<Pair<String, List<AppNotification>>> =
    notifications
        .groupBy { NotificationFormatters.group(it.occurredAt.toLocalDate(), now.toLocalDate()) }
        .toList()

/** Notice shown when part of the sources could not be read. */
@Composable
private fun PartialBanner() {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Algunas notificaciones no se pudieron cargar. Desliza hacia abajo para reintentar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m)
        )
    }
}

/**
 * Centered message for an empty list or a failed load.
 *
 * @param title short headline.
 * @param message explanation below the headline.
 * @param modifier modifier applied to the message.
 * @param actionLabel text of the action button, or null for no button.
 * @param onAction called when the button is tapped.
 */
@Composable
private fun MessageState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.xl),
        verticalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Button(onClick = onAction, modifier = Modifier.padding(top = Spacing.s)) { Text(actionLabel) }
        }
    }
}
