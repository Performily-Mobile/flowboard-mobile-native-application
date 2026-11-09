package com.performily.flowboard.features.dashboard.infrastructure.mapper

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.benefits.domain.entity.EmployeeBenefits
import com.performily.flowboard.features.benefits.domain.entity.SyncedVacationBalance
import com.performily.flowboard.features.dashboard.domain.entity.AttendanceDayStatus
import com.performily.flowboard.features.dashboard.domain.entity.BenefitReminder
import com.performily.flowboard.features.dashboard.domain.entity.BenefitsOverview
import com.performily.flowboard.features.dashboard.domain.entity.DailyAttendance
import com.performily.flowboard.features.dashboard.domain.entity.EmployeeProfile
import com.performily.flowboard.features.dashboard.domain.entity.MyRequest
import com.performily.flowboard.features.dashboard.domain.entity.MyRequestStatus
import com.performily.flowboard.features.dashboard.domain.entity.VacationOverview
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import com.performily.flowboard.features.workspace.domain.entity.Employee

/** Translates the models of other bounded contexts into the employee dashboard model. */
object EmployeeDashboardMapper {

    /** Converts a Workspace employee into the profile shown in the greeting. */
    fun toProfile(employee: Employee) = EmployeeProfile(
        firstName = employee.name.firstName,
        jobDescription = employee.jobDescription
    )

    /** Converts an Attendance record into a day of the dashboard. */
    fun toDailyAttendance(record: AttendanceRecord) = DailyAttendance(
        date = record.workDate,
        checkIn = record.checkInTime,
        checkOut = record.checkOutTime,
        effectiveHours = record.effectiveHours,
        overtimeHours = record.overtimeHours,
        status = toDayStatus(record.status)
    )

    /** Converts the vacation balance of Benefits into the dashboard overview. */
    fun toVacation(synced: SyncedVacationBalance) = VacationOverview(
        availableDays = synced.balance.availableDays,
        accruedDays = synced.balance.accruedDays,
        usedDays = synced.balance.usedDays,
        syncedAt = synced.syncedAt,
        fromCache = synced.fromCache
    )

    /** Converts a Request-context request into a request of the dashboard. */
    fun toMyRequest(request: Request): MyRequest {
        val period = request.period
        return MyRequest(
            id = request.id,
            typeName = request.requestTypeName,
            status = toRequestStatus(request.status),
            startDate = period?.startDate,
            endDate = period?.endDate,
            hours = period?.takeIf { it.hasHours }?.hours
        )
    }

    /** Converts the benefits of an employee into the dashboard overview. */
    fun toBenefits(benefits: EmployeeBenefits) = BenefitsOverview(
        currentCount = benefits.current.size,
        deliveredCount = benefits.delivered.size,
        nextToExpire = benefits.current
            .minByOrNull { it.endDate }
            ?.let { BenefitReminder(name = it.benefitTypeName, endDate = it.endDate) }
    )

    /** Maps the attendance status of Attendance to the dashboard status. */
    private fun toDayStatus(status: AttendanceStatus): AttendanceDayStatus = when (status) {
        AttendanceStatus.ON_TIME -> AttendanceDayStatus.ON_TIME
        AttendanceStatus.LATE -> AttendanceDayStatus.LATE
        AttendanceStatus.ABSENT -> AttendanceDayStatus.ABSENT
        AttendanceStatus.JUSTIFIED -> AttendanceDayStatus.JUSTIFIED
        AttendanceStatus.INCOMPLETE -> AttendanceDayStatus.INCOMPLETE
    }

    /** Maps the request status of Request to the dashboard status. */
    private fun toRequestStatus(status: RequestStatus): MyRequestStatus = when (status) {
        RequestStatus.IN_PROGRESS -> MyRequestStatus.PENDING
        RequestStatus.UNDER_REVIEW -> MyRequestStatus.UNDER_REVIEW
        RequestStatus.APPROVED -> MyRequestStatus.APPROVED
        RequestStatus.REJECTED -> MyRequestStatus.REJECTED
        RequestStatus.CANCELLED -> MyRequestStatus.CANCELLED
    }
}
