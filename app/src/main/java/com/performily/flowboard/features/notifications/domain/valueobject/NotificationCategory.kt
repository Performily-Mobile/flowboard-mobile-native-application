package com.performily.flowboard.features.notifications.domain.valueobject

/** Source of a notification, used to pick its icon and color. */
enum class NotificationCategory {
    /** A change in one of the own requests. */
    REQUEST,

    /** Requests waiting for the user to approve them. */
    APPROVAL,

    /** A payslip that was published or paid. */
    PAYSLIP,

    /** A benefit that was assigned, delivered or is about to expire. */
    BENEFIT
}
