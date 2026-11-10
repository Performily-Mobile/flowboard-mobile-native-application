package com.performily.flowboard.features.notifications.infrastructure.local

import android.content.Context
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.notifications.domain.repository.NotificationReadStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the identifiers of the read notices in the shared preferences of the app, one set per
 * employee, so two people that share a phone do not mix their read state.
 */
@Singleton
class SharedPreferencesNotificationReadStore @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val currentEmployee: CurrentEmployeeProvider
) : NotificationReadStore {

    private val preferences by lazy { context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE) }

    override fun readIds(): Set<String> = preferences.getStringSet(key(), null)?.toSet().orEmpty()

    override fun markAsRead(ids: Collection<String>) {
        preferences.edit().putStringSet(key(), readIds() + ids).apply()
    }

    private fun key(): String = KEY_PREFIX + currentEmployee.currentEmployeeId().value

    private companion object {
        const val FILE_NAME = "flowboard_notifications"
        const val KEY_PREFIX = "read_"
    }
}
