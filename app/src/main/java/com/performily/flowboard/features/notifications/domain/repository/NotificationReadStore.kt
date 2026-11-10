package com.performily.flowboard.features.notifications.domain.repository

/** Remembers on the device which notices the signed-in user already opened. */
interface NotificationReadStore {

    /** Identifiers of the notices already read by the signed-in user. */
    fun readIds(): Set<String>

    /**
     * Marks notices as read.
     *
     * @param ids identifiers of the notices.
     */
    fun markAsRead(ids: Collection<String>)
}
