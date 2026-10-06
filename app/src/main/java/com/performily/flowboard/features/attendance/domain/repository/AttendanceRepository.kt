package com.performily.flowboard.features.attendance.domain.repository

import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.entity.AttendanceAreaReport
import com.performily.flowboard.features.attendance.domain.entity.AttendanceEmployee
import com.performily.flowboard.features.attendance.domain.entity.AttendanceHoursReport
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.domain.valueobject.PunchType

interface AttendanceRepository {
    suspend fun getMyAttendance(period: AttendancePeriod): Result<List<AttendanceRecord>>
    suspend fun getEmployeeAttendance(employeeId: Long, period: AttendancePeriod): Result<List<AttendanceRecord>>
    suspend fun getAreaAttendance(areaId: Long, period: AttendancePeriod): Result<AttendanceAreaReport>
    suspend fun getHoursReport(period: AttendancePeriod, areaId: Long?, orderByOvertime: Boolean): Result<AttendanceHoursReport>
    suspend fun registerPunch(type: PunchType): Result<AttendanceRecord?>
    suspend fun justifyAttendance(attendanceRecordId: Long, reason: String, evidenceUrl: String?): Result<AttendanceRecord>
    suspend fun getAreas(): Result<List<AttendanceArea>>
    suspend fun searchEmployees(query: String): Result<List<AttendanceEmployee>>
}
