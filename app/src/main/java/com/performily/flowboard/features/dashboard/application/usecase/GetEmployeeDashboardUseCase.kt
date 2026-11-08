package com.performily.flowboard.features.dashboard.application.usecase

import com.performily.flowboard.features.dashboard.domain.entity.AttendanceDayStatus
import com.performily.flowboard.features.dashboard.domain.entity.DailyAttendance
import com.performily.flowboard.features.dashboard.domain.entity.EmployeeDashboard
import com.performily.flowboard.features.dashboard.domain.entity.MonthAttendance
import com.performily.flowboard.features.dashboard.domain.entity.TodayAttendance
import com.performily.flowboard.features.dashboard.domain.repository.EmployeeDashboardRepository
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Assembles the dashboard of the signed-in employee by querying every source in parallel.
 *
 * If one source fails, only its section is left empty and the rest is still shown.
 */
class GetEmployeeDashboardUseCase @Inject constructor(
    private val repository: EmployeeDashboardRepository
) {

    /**
     * Builds the dashboard.
     *
     * @param today reference date for today's attendance and the month summary.
     * @return the dashboard, where failed sections are null.
     */
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): EmployeeDashboard = coroutineScope {
        val profile = async { repository.getProfile().getOrNull() }
        val attendance = async { repository.getAttendance(today.withDayOfMonth(1), today).getOrNull() }
        val vacation = async { repository.getVacation().getOrNull() }
        val requests = async { repository.getRequests().getOrNull() }
        val benefits = async { repository.getBenefits().getOrNull() }

        val records = attendance.await()
        val employee = profile.await()

        EmployeeDashboard(
            firstName = employee?.firstName,
            jobDescription = employee?.jobDescription,
            today = records?.let { TodayAttendance(today, it.firstOrNull { day -> day.date == today }) },
            month = records?.let(::summarizeMonth),
            vacation = vacation.await(),
            requests = requests.await(),
            benefits = benefits.await(),
            loadedAt = LocalDateTime.now()
        )
    }

    /**
     * Counts the days of the month by status and adds up the worked and overtime hours.
     *
     * @param days attendance records of the month.
     * @return the month summary.
     */
    private fun summarizeMonth(days: List<DailyAttendance>): MonthAttendance =
        MonthAttendance(
            onTime = days.count { it.status == AttendanceDayStatus.ON_TIME },
            late = days.count { it.status == AttendanceDayStatus.LATE },
            absent = days.count { it.status == AttendanceDayStatus.ABSENT },
            justified = days.count { it.status == AttendanceDayStatus.JUSTIFIED },
            incomplete = days.count { it.status == AttendanceDayStatus.INCOMPLETE },
            workedHours = days.sumOf { it.effectiveHours ?: 0.0 },
            overtimeHours = days.sumOf { it.overtimeHours ?: 0.0 }
        )
}
