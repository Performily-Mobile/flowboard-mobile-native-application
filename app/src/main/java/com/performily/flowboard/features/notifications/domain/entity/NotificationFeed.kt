package com.performily.flowboard.features.notifications.domain.entity

/**
 * Notices of the signed-in user.
 *
 * @property notifications notices, the most recent first.
 * @property hasMissingSources true when some source could not be read, so the list may be incomplete.
 */
data class NotificationFeed(
    val notifications: List<AppNotification>,
    val hasMissingSources: Boolean
) {

    /** Number of notices the user has not opened yet. */
    val unreadCount: Int get() = notifications.count { !it.isRead }
}
