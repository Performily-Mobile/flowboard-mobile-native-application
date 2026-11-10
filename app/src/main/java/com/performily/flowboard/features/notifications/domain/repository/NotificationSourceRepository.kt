package com.performily.flowboard.features.notifications.domain.repository

import com.performily.flowboard.features.notifications.domain.entity.NotificationFeed

/**
 * Builds the notices of the signed-in user from the data of the other bounded contexts.
 * The backend has no notification endpoint, so every notice comes from a fact already stored there.
 */
interface NotificationSourceRepository {

    /**
     * Reads the sources and turns their recent facts into notices, without the read state.
     *
     * @return the notices, or a failure when no source could be read.
     */
    suspend fun getNotifications(): Result<NotificationFeed>
}
