package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.entity.AttendanceHoursReport
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import javax.inject.Inject

class GetAttendanceHoursUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        period: AttendancePeriod,
        areaId: Long?,
        orderByOvertime: Boolean
    ): Result<AttendanceHoursReport> = repository.getHoursReport(period, areaId, orderByOvertime)
}
