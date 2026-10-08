package com.performily.flowboard.features.dashboard.infrastructure.remote

/**
 * Attendance counts of an area as returned by the backend.
 *
 * Only the counters the dashboard needs are declared; the other fields of the response are ignored.
 */
data class DashboardAttendanceSummaryDto(
    val areaId: Long,
    val onTime: Long,
    val late: Long,
    val absent: Long,
    val incomplete: Long,
    val justified: Long
)
