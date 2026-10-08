package com.performily.flowboard.features.dashboard.domain.repository

import com.performily.flowboard.features.dashboard.domain.entity.AttendanceSummary
import com.performily.flowboard.features.dashboard.domain.entity.DashboardArea
import com.performily.flowboard.features.dashboard.domain.entity.RequestToAttend
import java.math.BigDecimal
import java.time.LocalDate

/**
 * Data the dashboard takes from other bounded contexts.
 *
 * The dashboard owns no data of its own: it only reads and summarizes.
 */
interface HrDashboardRepository {

    /** Returns the first name of the signed-in user. */
    suspend fun getCurrentUserFirstName(): Result<String>

    /** Returns the active areas with their number of active employees. */
    suspend fun getActiveAreas(): Result<List<DashboardArea>>

    /**
     * Returns the attendance counts of an area in a date range.
     *
     * @param areaId identifier of the area.
     * @param from first day of the range, inclusive.
     * @param to last day of the range, inclusive.
     */
    suspend fun getAttendanceSummary(areaId: Long, from: LocalDate, to: LocalDate): Result<AttendanceSummary>

    /** Returns the requests pending in the HR inbox, oldest first. */
    suspend fun getRequestsToAttend(): Result<List<RequestToAttend>>

    /** Returns the available vacation days of every active employee. */
    suspend fun getAvailableVacationDays(): Result<List<BigDecimal>>
}
