package com.performily.flowboard.features.dashboard.application.usecase

import com.performily.flowboard.features.dashboard.domain.entity.AreaAttendance
import com.performily.flowboard.features.dashboard.domain.entity.DashboardArea
import com.performily.flowboard.features.dashboard.domain.entity.HrDashboard
import com.performily.flowboard.features.dashboard.domain.entity.MonthlyLateness
import com.performily.flowboard.features.dashboard.domain.repository.HrDashboardRepository
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

/**
 * Assembles the HR dashboard by querying every source in parallel.
 *
 * If one source fails, only its section is left empty and the rest is still shown.
 */
class GetHrDashboardUseCase @Inject constructor(
    private val repository: HrDashboardRepository
) {

    /**
     * Builds the dashboard.
     *
     * @param today reference date for the daily attendance and the monthly lateness.
     * @return the dashboard, where failed sections are null.
     */
    suspend operator fun invoke(today: LocalDate = LocalDate.now()): HrDashboard = coroutineScope {
        val firstName = async { repository.getCurrentUserFirstName().getOrNull() }
        val requests = async { repository.getRequestsToAttend().getOrNull() }
        val vacationDays = async { repository.getAvailableVacationDays().getOrNull() }
        val areas = repository.getActiveAreas().getOrNull()

        val todayAttendance = async { areas?.let { todayAttendance(it, today) } }
        val monthlyLateness = async { areas?.let { monthlyLateness(it, today) } }
        val pending = requests.await()

        HrDashboard(
            userFirstName = firstName.await(),
            activeEmployees = areas?.sumOf { it.activeEmployees },
            pendingRequests = pending?.size,
            monthlyLateness = monthlyLateness.await(),
            expiringVacations = vacationDays.await()?.count { it >= EXPIRING_VACATION_DAYS },
            todayAttendance = todayAttendance.await(),
            requestsToAttend = pending?.take(MAX_REQUESTS_TO_ATTEND),
            loadedAt = LocalDateTime.now()
        )
    }

    /**
     * Computes, per area, the employees who checked in today over the area's active employees.
     *
     * Active employees are used as the denominator instead of attendance records because absences
     * are only registered when the day closes, which would inflate the percentage.
     *
     * @return the list per area, or null when every area failed to load.
     */
    private suspend fun todayAttendance(areas: List<DashboardArea>, today: LocalDate): List<AreaAttendance>? =
        coroutineScope {
            val results = areas.filter { it.activeEmployees > 0 }.map { area ->
                async {
                    repository.getAttendanceSummary(area.id, today, today).map { summary ->
                        AreaAttendance(
                            areaId = area.id,
                            areaName = area.name,
                            percentage = (summary.checkIns * 100.0 / area.activeEmployees).roundToInt().coerceIn(0, 100)
                        )
                    }
                }
            }.awaitAll()
            if (results.isNotEmpty() && results.all { it.isFailure }) null else results.mapNotNull { it.getOrNull() }
        }

    /**
     * Adds up the late check-ins from the first day of the month until today across all areas.
     *
     * @return the monthly lateness, or null when every area failed to load.
     */
    private suspend fun monthlyLateness(areas: List<DashboardArea>, today: LocalDate): MonthlyLateness? =
        coroutineScope {
            val results = areas.map { area ->
                async { repository.getAttendanceSummary(area.id, today.withDayOfMonth(1), today) }
            }.awaitAll()
            if (results.isNotEmpty() && results.all { it.isFailure }) {
                null
            } else {
                val summaries = results.mapNotNull { it.getOrNull() }
                MonthlyLateness(
                    lateCount = summaries.sumOf { it.late },
                    checkInCount = summaries.sumOf { it.checkIns }
                )
            }
        }

    private companion object {
        /** A full year of unused vacation: it must be taken before it expires. */
        val EXPIRING_VACATION_DAYS: BigDecimal = BigDecimal(30)
        const val MAX_REQUESTS_TO_ATTEND = 3
    }
}
