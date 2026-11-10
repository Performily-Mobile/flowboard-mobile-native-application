package com.performily.flowboard.features.notifications.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.performily.flowboard.features.notifications.application.usecase.GetNotificationsUseCase
import com.performily.flowboard.features.notifications.application.usecase.MarkNotificationsAsReadUseCase
import com.performily.flowboard.features.notifications.domain.entity.AppNotification
import com.performily.flowboard.features.notifications.presentation.state.NotificationFilter
import com.performily.flowboard.features.notifications.presentation.state.NotificationsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Holds the notices of the signed-in user and their read state. */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markAsRead: MarkNotificationsAsReadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    /** Loads the notices when the screen opens, unless a request is already running. */
    fun load() = fetch(isRefresh = false)

    /** Reloads the notices after a pull-to-refresh or a retry. */
    fun refresh() = fetch(isRefresh = true)

    /**
     * Changes the filter of the list.
     *
     * @param filter filter picked by the user.
     */
    fun selectFilter(filter: NotificationFilter) {
        _state.update { it.copy(filter = filter) }
    }

    /**
     * Marks a notice as read when the user opens it.
     *
     * @param notification notice that was tapped.
     */
    fun onOpened(notification: AppNotification) {
        if (notification.isRead) return
        markRead(setOf(notification.id))
    }

    /** Marks every notice as read. */
    fun markAllAsRead() {
        markRead(_state.value.notifications.filterNot { it.isRead }.map { it.id }.toSet())
    }

    /**
     * Saves the read state and updates the list right away.
     *
     * @param ids identifiers of the notices to mark.
     */
    private fun markRead(ids: Set<String>) {
        if (ids.isEmpty()) return
        markAsRead(ids)
        _state.update { current ->
            current.copy(
                notifications = current.notifications.map {
                    if (it.id in ids) it.copy(isRead = true) else it
                }
            )
        }
    }

    /**
     * Fetches the notices unless a request is already running.
     *
     * @param isRefresh true when the user asked for the reload explicitly.
     */
    private fun fetch(isRefresh: Boolean) {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = !isRefresh && it.notifications.isEmpty(),
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }
            getNotifications().fold(
                onSuccess = { feed ->
                    _state.update {
                        it.copy(
                            notifications = feed.notifications,
                            hasMissingSources = feed.hasMissingSources,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(isLoading = false, isRefreshing = false, errorMessage = LOAD_ERROR)
                    }
                }
            )
        }
    }

    private companion object {
        const val LOAD_ERROR = "No se pudieron cargar tus notificaciones. Revisa tu conexión e inténtalo de nuevo."
    }
}
