package com.performily.flowboard.features.dashboard.domain.repository

import com.performily.flowboard.features.dashboard.domain.entity.BenefitsOverview
import com.performily.flowboard.features.dashboard.domain.entity.DailyAttendance
import com.performily.flowboard.features.dashboard.domain.entity.EmployeeProfile
import com.performily.flowboard.features.dashboard.domain.entity.RequestsOverview
import com.performily.flowboard.features.dashboard.domain.entity.VacationOverview
import java.time.LocalDate

/**
 * Own data of the signed-in employee that the dashboard takes from other bounded contexts.
 *
 * The dashboard owns no data of its own: it only reads and summarizes.
 */
interface EmployeeDashboardRepository {

    /** Returns the name and job of the signed-in employee. */
    suspend fun getProfile(): Result<EmployeeProfile>

    /**
     * Returns the attendance records of the signed-in employee in a date range.
     *
     * @param from first day of the range, inclusive.
     * @param to last day of the range, inclusive.
     */
    suspend fun getAttendance(from: LocalDate, to: LocalDate): Result<List<DailyAttendance>>

    /** Returns the vacation balance of the signed-in employee. */
    suspend fun getVacation(): Result<VacationOverview>

    /** Returns the request counters and the latest requests of the signed-in employee. */
    suspend fun getRequests(): Result<RequestsOverview>

    /** Returns the benefits assigned to the signed-in employee. */
    suspend fun getBenefits(): Result<BenefitsOverview>
}
