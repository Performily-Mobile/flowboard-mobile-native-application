package com.performily.flowboard.features.dashboard.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.features.benefits.application.usecase.GetVacationBalancesUseCase
import com.performily.flowboard.features.dashboard.domain.entity.AttendanceSummary
import com.performily.flowboard.features.dashboard.domain.entity.DashboardArea
import com.performily.flowboard.features.dashboard.domain.entity.RequestToAttend
import com.performily.flowboard.features.dashboard.domain.repository.HrDashboardRepository
import com.performily.flowboard.features.dashboard.infrastructure.mapper.DashboardMapper
import com.performily.flowboard.features.dashboard.infrastructure.remote.DashboardAttendanceService
import com.performily.flowboard.features.request.application.usecase.GetPendingApprovalsUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetAreasUseCase
import com.performily.flowboard.features.workspace.application.usecase.GetCurrentEmployeeUseCase
import java.math.BigDecimal
import java.time.LocalDate
import javax.inject.Inject

/**
 * Reads from Workspace, Request and Benefits through their use cases, and from Attendance
 * through its per-area summary endpoint.
 */
class HrDashboardRepositoryImpl @Inject constructor(
    private val getCurrentEmployee: GetCurrentEmployeeUseCase,
    private val getAreas: GetAreasUseCase,
    private val getPendingApprovals: GetPendingApprovalsUseCase,
    private val getVacationBalances: GetVacationBalancesUseCase,
    private val attendanceService: DashboardAttendanceService
) : HrDashboardRepository {

    override suspend fun getCurrentUserFirstName(): Result<String> =
        getCurrentEmployee().map { it.name.firstName }

    override suspend fun getActiveAreas(): Result<List<DashboardArea>> =
        getAreas(onlyActive = true).map { areas -> areas.map(DashboardMapper::toArea) }

    override suspend fun getAttendanceSummary(
        areaId: Long,
        from: LocalDate,
        to: LocalDate
    ): Result<AttendanceSummary> =
        apiCall { attendanceService.getAreaSummary(areaId, from.toString(), to.toString()) }
            .map(DashboardMapper::toSummary)

    override suspend fun getRequestsToAttend(): Result<List<RequestToAttend>> =
        getPendingApprovals().map { requests ->
            requests.sortedBy { it.submittedAt }.map(DashboardMapper::toRequestToAttend)
        }

    override suspend fun getAvailableVacationDays(): Result<List<BigDecimal>> =
        getVacationBalances().map { balances -> balances.map { it.availableDays } }
}
