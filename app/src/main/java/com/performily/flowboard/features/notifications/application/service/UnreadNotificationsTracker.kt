package com.performily.flowboard.features.notifications.application.service

import com.performily.flowboard.features.notifications.application.usecase.GetNotificationsUseCase
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of the number of unread notices for the whole app.
 *
 * The notification screen reports every change (load, open, "Marcar leídas") and the dashboards
 * read [count] for the bell badge, so the bell clears as soon as the user reads the notices
 * instead of reflecting an unrelated figure such as pending requests.
 */
@Singleton
class UnreadNotificationsTracker @Inject constructor(
    private val getNotifications: GetNotificationsUseCase
) {

    private val _count = MutableStateFlow(0)

    /** Number of notices the signed-in user has not read yet. */
    val count: StateFlow<Int> = _count.asStateFlow()

    /** Reads the notices again and updates [count]; a failure keeps the last known value. */
    suspend fun refresh() {
        getNotifications().onSuccess { feed -> _count.value = feed.unreadCount }
    }

    /**
     * Sets the count from a list that is already loaded.
     *
     * @param unread number of unread notices.
     */
    fun update(unread: Int) {
        _count.value = unread
    }
}
