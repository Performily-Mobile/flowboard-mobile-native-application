package com.performily.flowboard.features.attendance.application.usecase

import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import javax.inject.Inject

class JustifyAttendanceUseCase @Inject constructor(
    private val repository: AttendanceRepository
) {
    suspend operator fun invoke(
        attendanceRecordId: Long,
        reason: String,
        evidenceUrl: String?
    ): Result<AttendanceRecord> = repository.justifyAttendance(attendanceRecordId, reason, evidenceUrl)
}
