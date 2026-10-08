package com.performily.flowboard.features.dashboard.domain.entity

/**
 * Active area with its number of active employees (Workspace).
 *
 * @property id identifier of the area.
 * @property name display name of the area.
 * @property activeEmployees employees currently active in the area.
 */
data class DashboardArea(
    val id: Long,
    val name: String,
    val activeEmployees: Long
)

/**
 * Count of attendance records of an area in a period (Attendance).
 *
 * @property onTime records checked in on time.
 * @property late records checked in late.
 * @property absent records marked as absent.
 * @property incomplete records with a check-in but no check-out yet.
 * @property justified records whose absence or lateness was justified.
 */
data class AttendanceSummary(
    val onTime: Long,
    val late: Long,
    val absent: Long,
    val incomplete: Long,
    val justified: Long
) {

    /** Records with a check-in: on time, late or still without check-out. */
    val checkIns: Long get() = onTime + late + incomplete
}
