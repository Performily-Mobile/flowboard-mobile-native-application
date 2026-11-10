package com.performily.flowboard.features.notifications.application.usecase

import com.performily.flowboard.features.notifications.domain.repository.NotificationReadStore
import javax.inject.Inject

/** Marks notices of the signed-in user as read on this device. */
class MarkNotificationsAsReadUseCase @Inject constructor(
    private val readStore: NotificationReadStore
) {

    /**
     * @param ids identifiers of the notices to mark.
     * @return a failure when the read state could not be saved.
     */
    operator fun invoke(ids: Collection<String>): Result<Unit> = runCatching {
        if (ids.isNotEmpty()) readStore.markAsRead(ids)
    }
}
