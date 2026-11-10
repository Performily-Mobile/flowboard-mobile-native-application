package com.performily.flowboard.features.notifications.domain.valueobject

/** Screen a notification opens when it is tapped. */
sealed interface NotificationTarget {

    /**
     * Detail of one of the own requests.
     *
     * @property requestId identifier of the request.
     */
    data class Request(val requestId: Long) : NotificationTarget

    /** Requests waiting for the user to approve them. */
    data object PendingApprovals : NotificationTarget

    /**
     * Viewer of one of the own payslips.
     *
     * @property payslipId identifier of the payslip.
     */
    data class Payslip(val payslipId: Long) : NotificationTarget

    /** Own benefits. */
    data object MyBenefits : NotificationTarget
}
