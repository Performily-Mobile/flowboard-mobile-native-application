package com.performily.flowboard.features.notifications.presentation.state

import com.performily.flowboard.features.notifications.domain.entity.AppNotification

/**
 * Filter of the notification list.
 *
 * @property label text of the tab.
 */
enum class NotificationFilter(val label: String) {
    ALL("Todas"),
    UNREAD("No leídas")
}

/**
 * State of the notification screen.
 *
 * @property notifications every notice, the most recent first.
 * @property filter filter selected by the user.
 * @property isLoading true during the first load.
 * @property isRefreshing true while a pull-to-refresh runs.
 * @property errorMessage reason the notices could not be loaded, or null.
 * @property hasMissingSources true when part of the notices could not be loaded.
 */
data class NotificationsUiState(
    val notifications: List<AppNotification> = emptyList(),
    val filter: NotificationFilter = NotificationFilter.ALL,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val hasMissingSources: Boolean = false
) {

    /** Number of notices not opened yet. */
    val unreadCount: Int get() = notifications.count { !it.isRead }

    /** Notices that match the selected filter. */
    val visibleNotifications: List<AppNotification>
        get() = when (filter) {
            NotificationFilter.ALL -> notifications
            NotificationFilter.UNREAD -> notifications.filterNot { it.isRead }
        }
}
