package com.performily.flowboard.features.notifications.application.usecase

import com.performily.flowboard.features.notifications.domain.entity.NotificationFeed
import com.performily.flowboard.features.notifications.domain.repository.NotificationReadStore
import com.performily.flowboard.features.notifications.domain.repository.NotificationSourceRepository
import javax.inject.Inject

/** Gets the notices of the signed-in user with their read state, the most recent first. */
class GetNotificationsUseCase @Inject constructor(
    private val sources: NotificationSourceRepository,
    private val readStore: NotificationReadStore
) {

    /** @return the notices, or a failure when none of their sources could be read. */
    suspend operator fun invoke(): Result<NotificationFeed> =
        sources.getNotifications().mapCatching { feed ->
            val readIds = readStore.readIds()
            feed.copy(
                notifications = feed.notifications
                    .map { it.copy(isRead = it.id in readIds) }
                    .sortedByDescending { it.occurredAt }
            )
        }
}
