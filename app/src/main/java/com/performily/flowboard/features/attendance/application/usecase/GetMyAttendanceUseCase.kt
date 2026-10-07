package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import javax.inject.Inject

class GetMyAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(period: AttendancePeriod): Result<List<AttendanceRecord>> =
        repository.getMyAttendance(period)
}
