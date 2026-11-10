package com.performily.flowboard.features.notifications.domain.entity

import com.performily.flowboard.features.notifications.domain.valueobject.NotificationCategory
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationTarget
import java.time.LocalDateTime

/**
 * Notice shown to the signed-in user, built from their own requests, payslips and benefits.
 *
 * @property id stable key of the notice; it changes when the fact behind it changes, so a new fact
 * shows up as unread again.
 * @property category source of the notice.
 * @property title short headline.
 * @property message one or two sentences with the detail.
 * @property occurredAt moment the fact happened, used to sort the notices.
 * @property target screen opened when the notice is tapped.
 * @property isRead true once the user opened it or marked every notice as read.
 */
data class AppNotification(
    val id: String,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val occurredAt: LocalDateTime,
    val target: NotificationTarget,
    val isRead: Boolean = false
)
