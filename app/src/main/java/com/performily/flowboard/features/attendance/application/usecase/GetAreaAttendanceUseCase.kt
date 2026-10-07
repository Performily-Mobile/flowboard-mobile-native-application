package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.entity.AttendanceAreaReport
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import javax.inject.Inject

class GetAreaAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(areaId: Long, period: AttendancePeriod): Result<AttendanceAreaReport> =
        repository.getAreaAttendance(areaId, period)
}
