package com.performily.flowboard.features.dashboard.infrastructure.repository

import com.performily.flowboard.features.attendance.application.usecase.GetMyAttendanceUseCase
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.benefits.application.usecase.GetMyBenefitsUseCase
import com.performily.flowboard.features.benefits.application.usecase.GetMyVacationBalanceUseCase
import com.performily.flowboard.features.dashboard.domain.entity.BenefitsOverview
import com.performily.flowboard.features.dashboard.domain.entity.DailyAttendance
import com.performily.flowboard.features.dashboard.domain.entity.EmployeeProfile
import com.performily.flowboard.features.dashboard.domain.entity.RequestsOverview
import com.performily.flowboard.features.dashboard.domain.entity.VacationOverview
import com.performily.flowboard.features.dashboard.domain.repository.EmployeeDashboardRepository
import com.performily.flowboard.features.dashboard.infrastructure.mapper.EmployeeDashboardMapper
import com.performily.flowboard.features.request.application.usecase.GetMyRequestsUseCase
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus
import com.performily.flowboard.features.workspace.application.usecase.GetCurrentEmployeeUseCase
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * Reads the own data of the signed-in employee from Workspace, Attendance, Benefits and Request
 * through their use cases.
 */
class EmployeeDashboardRepositoryImpl @Inject constructor(
    private val getCurrentEmployee: GetCurrentEmployeeUseCase,
    private val getMyAttendance: GetMyAttendanceUseCase,
    private val getMyVacationBalance: GetMyVacationBalanceUseCase,
    private val getMyRequests: GetMyRequestsUseCase,
    private val getMyBenefits: GetMyBenefitsUseCase
) : EmployeeDashboardRepository {

    override suspend fun getProfile(): Result<EmployeeProfile> =
        getCurrentEmployee().map(EmployeeDashboardMapper::toProfile)

    override suspend fun getAttendance(from: LocalDate, to: LocalDate): Result<List<DailyAttendance>> =
        getMyAttendance(AttendancePeriod(from, to)).map { records ->
            records.map(EmployeeDashboardMapper::toDailyAttendance)
        }

    override suspend fun getVacation(): Result<VacationOverview> =
        getMyVacationBalance().map(EmployeeDashboardMapper::toVacation)

    /**
     * Counts the pending and returned requests with the total of their own queries and takes the
     * latest requests from an unfiltered query. If any of the three fails the whole section fails,
     * so a wrong zero is never shown.
     */
    override suspend fun getRequests(): Result<RequestsOverview> = coroutineScope {
        val pending = async { getMyRequests(RequestStatus.IN_PROGRESS) }
        val underReview = async { getMyRequests(RequestStatus.UNDER_REVIEW) }
        val recent = async { getMyRequests() }

        val pendingPage = pending.await()
        val underReviewPage = underReview.await()
        val recentPage = recent.await()

        val failure = listOf(pendingPage, underReviewPage, recentPage).firstNotNullOfOrNull { it.exceptionOrNull() }
        if (failure != null) {
            Result.failure(failure)
        } else {
            Result.success(
                RequestsOverview(
                    pending = pendingPage.getOrThrow().totalItems.toInt(),
                    underReview = underReviewPage.getOrThrow().totalItems.toInt(),
                    recent = recentPage.getOrThrow().items
                        .map(EmployeeDashboardMapper::toMyRequest)
                        .take(MAX_RECENT_REQUESTS)
                )
            )
        }
    }

    override suspend fun getBenefits(): Result<BenefitsOverview> =
        getMyBenefits().map(EmployeeDashboardMapper::toBenefits)

    private companion object {
        /** Number of latest requests shown in the dashboard. */
        const val MAX_RECENT_REQUESTS = 3
    }
}
