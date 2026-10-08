package com.performily.flowboard.features.dashboard.domain.entity

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * HR dashboard indicators (MA-18).
 *
 * Every section is loaded independently: a null value means that section could not be obtained.
 *
 * @property userFirstName first name of the signed-in user, used in the greeting.
 * @property activeEmployees total of active employees across all active areas.
 * @property pendingRequests number of requests waiting in the HR inbox.
 * @property monthlyLateness late check-ins of the current month against all check-ins.
 * @property expiringVacations number of employees with a full year of unused vacation.
 * @property todayAttendance share of active employees per area that checked in today.
 * @property requestsToAttend oldest pending requests shown in the dashboard.
 * @property loadedAt moment the dashboard was assembled.
 */
data class HrDashboard(
    val userFirstName: String?,
    val activeEmployees: Long?,
    val pendingRequests: Int?,
    val monthlyLateness: MonthlyLateness?,
    val expiringVacations: Int?,
    val todayAttendance: List<AreaAttendance>?,
    val requestsToAttend: List<RequestToAttend>?,
    val loadedAt: LocalDateTime
) {

    /** True when every section failed to load, so there is nothing to show. */
    val hasNoData: Boolean
        get() = activeEmployees == null && pendingRequests == null && monthlyLateness == null &&
                expiringVacations == null && todayAttendance == null && requestsToAttend == null
}

/**
 * Late check-ins of the month against the total of check-ins.
 *
 * @property lateCount check-ins registered after the scheduled time.
 * @property checkInCount all check-ins in the period.
 */
data class MonthlyLateness(
    val lateCount: Long,
    val checkInCount: Long
) {

    /** Late check-ins as a percentage of all check-ins, or 0 when there are none. */
    val percentage: Double
        get() = if (checkInCount == 0L) 0.0 else lateCount * 100.0 / checkInCount
}

/**
 * Share of the active employees of an area that checked in today.
 *
 * @property areaId identifier of the area.
 * @property areaName display name of the area.
 * @property percentage value from 0 to 100.
 */
data class AreaAttendance(
    val areaId: Long,
    val areaName: String,
    val percentage: Int
)

/**
 * Pending request in the HR inbox.
 *
 * @property requestId identifier of the request.
 * @property requesterName full name of the employee who submitted it.
 * @property requestTypeName name of the request type, for example "Vacaciones".
 * @property startDate first day covered by the request, if it has a period.
 * @property endDate last day covered by the request, if it has a period.
 * @property hours duration in hours for hourly permissions, null otherwise.
 * @property withoutDirectManager true when the requester has no direct manager assigned.
 */
data class RequestToAttend(
    val requestId: Long,
    val requesterName: String,
    val requestTypeName: String,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val hours: Double?,
    val withoutDirectManager: Boolean
)
