package com.performily.flowboard.features.attendance.infrastructure.mapper

import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.entity.AttendanceEmployee
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.infrastructure.remote.AreaDto
import com.performily.flowboard.features.attendance.infrastructure.remote.AttendanceRecordDto
import com.performily.flowboard.features.attendance.infrastructure.remote.EmployeeSummaryDto
import java.time.LocalDate
import java.time.LocalTime

object AttendanceMapper {

    fun toDomain(dto: AttendanceRecordDto): AttendanceRecord {
        val resolvedName = dto.employeeName
            ?: listOfNotNull(dto.firstName, dto.lastName).joinToString(" ").trim().ifBlank { null }

        return AttendanceRecord(
            id = dto.id,
            employeeId = dto.employeeId,
            employeeName = resolvedName,
            areaId = dto.areaId,
            areaName = dto.areaName,
            positionTitle = dto.positionTitle,
            workDate = LocalDate.parse(dto.workDate),
            checkInTime = dto.checkInTime?.toLocalTimeOrNull(),
            checkOutTime = dto.checkOutTime?.toLocalTimeOrNull(),
            effectiveHours = dto.effectiveHours ?: dto.workedHours,
            overtimeHours = dto.overtimeHours ?: dto.overtime,
            status = runCatching { AttendanceStatus.valueOf(dto.status) }
                .getOrDefault(AttendanceStatus.INCOMPLETE)
        )
    }

    fun toArea(dto: AreaDto): AttendanceArea = AttendanceArea(dto.id, dto.name)

    fun toEmployee(dto: EmployeeSummaryDto): AttendanceEmployee = AttendanceEmployee(
        id = dto.id,
        fullName = dto.fullName
            ?: listOfNotNull(dto.firstName, dto.lastName).joinToString(" ").trim(),
        areaId = dto.areaId,
        areaName = dto.areaName
    )

    private fun String.toLocalTimeOrNull(): LocalTime? = runCatching {
        LocalTime.parse(this)
    }.getOrNull()
}
