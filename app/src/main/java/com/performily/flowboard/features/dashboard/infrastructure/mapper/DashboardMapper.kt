package com.performily.flowboard.features.dashboard.infrastructure.mapper

import com.performily.flowboard.features.dashboard.domain.entity.AttendanceSummary
import com.performily.flowboard.features.dashboard.domain.entity.DashboardArea
import com.performily.flowboard.features.dashboard.domain.entity.RequestToAttend
import com.performily.flowboard.features.dashboard.infrastructure.remote.DashboardAttendanceSummaryDto
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.workspace.domain.entity.Area

/** Translates the models of other bounded contexts into the dashboard model. */
object DashboardMapper {

    /** Converts a Workspace area into a dashboard area. */
    fun toArea(area: Area) = DashboardArea(
        id = area.id,
        name = area.name,
        activeEmployees = area.activeEmployees
    )

    /** Converts the Attendance response into an attendance summary. */
    fun toSummary(dto: DashboardAttendanceSummaryDto) = AttendanceSummary(
        onTime = dto.onTime,
        late = dto.late,
        absent = dto.absent,
        incomplete = dto.incomplete,
        justified = dto.justified
    )

    /** Converts a Request-context request into a pending request of the dashboard. */
    fun toRequestToAttend(request: Request): RequestToAttend {
        val period = request.period
        return RequestToAttend(
            requestId = request.id,
            requesterName = request.requesterName,
            requestTypeName = request.requestTypeName,
            startDate = period?.startDate,
            endDate = period?.endDate,
            hours = period?.takeIf { it.hasHours }?.hours,
            withoutDirectManager = request.requester?.let { it.directManagerId == null } ?: false
        )
    }
}
