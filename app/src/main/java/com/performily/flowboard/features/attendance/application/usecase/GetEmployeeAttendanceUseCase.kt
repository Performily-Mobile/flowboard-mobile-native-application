package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import javax.inject.Inject

class GetEmployeeAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(employeeId: Long, period: AttendancePeriod): Result<List<AttendanceRecord>> =
        repository.getEmployeeAttendance(employeeId, period)
}
