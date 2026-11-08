package com.performily.flowboard.features.dashboard.domain.entity

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Dashboard of the signed-in employee, built only from their own data.
 *
 * Every section is loaded independently: a null value means that section could not be obtained.
 *
 * @property firstName first name of the employee, used in the greeting.
 * @property jobDescription position and area of the employee, for example "Analista · Finanzas".
 * @property today attendance of the current day.
 * @property month attendance summary from the first day of the month until today.
 * @property vacation vacation balance of the employee.
 * @property requests counters and latest requests of the employee.
 * @property benefits benefits currently assigned to the employee.
 * @property loadedAt moment the dashboard was assembled.
 */
data class EmployeeDashboard(
    val firstName: String?,
    val jobDescription: String?,
    val today: TodayAttendance?,
    val month: MonthAttendance?,
    val vacation: VacationOverview?,
    val requests: RequestsOverview?,
    val benefits: BenefitsOverview?,
    val loadedAt: LocalDateTime
) {

    /** True when every section failed to load, so there is nothing to show. */
    val hasNoData: Boolean
        get() = today == null && month == null && vacation == null && requests == null && benefits == null
}

/**
 * Basic data of the signed-in employee.
 *
 * @property firstName first name of the employee.
 * @property jobDescription position and area of the employee.
 */
data class EmployeeProfile(
    val firstName: String,
    val jobDescription: String
)

/** Status of one attendance day. */
enum class AttendanceDayStatus(val label: String) {
    ON_TIME("Puntual"),
    LATE("Tardanza"),
    ABSENT("Inasistencia"),
    JUSTIFIED("Justificada"),
    INCOMPLETE("Incompleto")
}

/**
 * Attendance record of one day of the employee.
 *
 * @property date day of the record.
 * @property checkIn time of the check-in, if the employee punched in.
 * @property checkOut time of the check-out, if the employee punched out.
 * @property effectiveHours worked hours of the day, if already computed.
 * @property overtimeHours overtime hours of the day, if any.
 * @property status status of the day.
 */
data class DailyAttendance(
    val date: LocalDate,
    val checkIn: LocalTime?,
    val checkOut: LocalTime?,
    val effectiveHours: Double?,
    val overtimeHours: Double?,
    val status: AttendanceDayStatus
)

/**
 * Attendance of the current day.
 *
 * @property date current day.
 * @property record record of the day, or null when the employee has not punched in yet.
 */
data class TodayAttendance(
    val date: LocalDate,
    val record: DailyAttendance?
) {

    /** True when the employee punched in and has not punched out yet. */
    val isWorking: Boolean
        get() = record?.checkIn != null && record.checkOut == null
}

/**
 * Attendance counters of the month.
 *
 * @property onTime days checked in on time.
 * @property late days checked in late.
 * @property absent days marked as absent.
 * @property justified days whose absence or lateness was justified.
 * @property incomplete days with a check-in but no check-out.
 * @property workedHours sum of the effective hours of the month.
 * @property overtimeHours sum of the overtime hours of the month.
 */
data class MonthAttendance(
    val onTime: Int,
    val late: Int,
    val absent: Int,
    val justified: Int,
    val incomplete: Int,
    val workedHours: Double,
    val overtimeHours: Double
) {

    /** Days with a registered status. */
    val totalDays: Int get() = onTime + late + absent + justified + incomplete

    /** Days with a check-in: on time, late or still without check-out. */
    val checkIns: Int get() = onTime + late + incomplete

    /** Share of the check-ins that were on time from 0 to 100, or null when there are none. */
    val punctualityPercentage: Int?
        get() = if (checkIns == 0) null else Math.round(onTime * 100.0 / checkIns).toInt()
}

/**
 * Vacation balance of the employee.
 *
 * @property availableDays days that can still be taken.
 * @property accruedDays days accumulated so far.
 * @property usedDays days already taken.
 * @property syncedAt moment the balance was obtained from the server.
 * @property fromCache true when the server could not be reached and the last saved copy is shown.
 */
data class VacationOverview(
    val availableDays: BigDecimal,
    val accruedDays: BigDecimal,
    val usedDays: BigDecimal,
    val syncedAt: LocalDateTime,
    val fromCache: Boolean
) {

    /** Part of the accumulated days already taken, from 0 to 1. */
    val usedRatio: Float
        get() = if (accruedDays.signum() <= 0) 0f else (usedDays.toFloat() / accruedDays.toFloat()).coerceIn(0f, 1f)
}

/** Status of a request of the employee. */
enum class MyRequestStatus(val label: String) {
    PENDING("Pendiente"),
    UNDER_REVIEW("En revisión"),
    APPROVED("Aprobada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada")
}

/**
 * Request submitted by the employee.
 *
 * @property id identifier of the request.
 * @property typeName name of the request type, for example "Vacaciones".
 * @property status current status of the request.
 * @property startDate first day covered by the request, if it has a period.
 * @property endDate last day covered by the request, if it has a period.
 * @property hours duration in hours for hourly permissions, null otherwise.
 */
data class MyRequest(
    val id: Long,
    val typeName: String,
    val status: MyRequestStatus,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val hours: Double?
)

/**
 * Requests of the employee.
 *
 * @property pending requests waiting for an answer.
 * @property underReview requests returned to the employee to be corrected and sent again.
 * @property recent latest requests, newest first.
 */
data class RequestsOverview(
    val pending: Int,
    val underReview: Int,
    val recent: List<MyRequest>
)

/**
 * Benefit that is about to end.
 *
 * @property name name of the benefit type.
 * @property endDate last day the benefit is valid.
 */
data class BenefitReminder(
    val name: String,
    val endDate: LocalDate
)

/**
 * Benefits of the employee.
 *
 * @property currentCount benefits assigned and not delivered yet.
 * @property deliveredCount benefits already delivered.
 * @property nextToExpire current benefit with the nearest end date, if any.
 */
data class BenefitsOverview(
    val currentCount: Int,
    val deliveredCount: Int,
    val nextToExpire: BenefitReminder?
)
