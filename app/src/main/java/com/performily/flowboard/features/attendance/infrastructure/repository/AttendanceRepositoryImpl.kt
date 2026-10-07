package com.performily.flowboard.features.attendance.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.attendance.domain.entity.AttendanceArea
import com.performily.flowboard.features.attendance.domain.entity.AttendanceAreaReport
import com.performily.flowboard.features.attendance.domain.entity.AttendanceEmployee
import com.performily.flowboard.features.attendance.domain.entity.AttendanceEmployeeSummary
import com.performily.flowboard.features.attendance.domain.entity.AttendanceHoursReport
import com.performily.flowboard.features.attendance.domain.entity.AttendanceRecord
import com.performily.flowboard.features.attendance.domain.entity.EmployeeHoursSummary
import com.performily.flowboard.features.attendance.domain.repository.AttendanceRepository
import com.performily.flowboard.features.attendance.domain.valueobject.AttendancePeriod
import com.performily.flowboard.features.attendance.domain.valueobject.AttendanceStatus
import com.performily.flowboard.features.attendance.domain.valueobject.PunchType
import com.performily.flowboard.features.attendance.infrastructure.mapper.AttendanceMapper
import com.performily.flowboard.features.attendance.infrastructure.remote.AttendanceService
import com.performily.flowboard.features.attendance.infrastructure.remote.AttendanceWorkspaceService
import com.performily.flowboard.features.attendance.infrastructure.remote.JustifyAttendanceRequestDto
import com.performily.flowboard.features.attendance.infrastructure.remote.PunchRequestDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.Instant
import javax.inject.Inject
import kotlin.math.round

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceService: AttendanceService,
    private val workspaceService: AttendanceWorkspaceService,
    private val currentEmployeeProvider: CurrentEmployeeProvider
) : AttendanceRepository {

    override suspend fun getMyAttendance(period: AttendancePeriod): Result<List<AttendanceRecord>> =
        apiCall { attendanceService.getMyAttendance(period.from.toString(), period.to.toString()) }
            .mapCatching { it.map(AttendanceMapper::toDomain) }

    override suspend fun getEmployeeAttendance(
        employeeId: Long,
        period: AttendancePeriod
    ): Result<List<AttendanceRecord>> =
        apiCall {
            attendanceService.getEmployeeAttendance(
                employeeId = employeeId,
                fromDate = period.from.toString(),
                toDate = period.to.toString()
            )
        }.mapCatching { it.map(AttendanceMapper::toDomain) }

    override suspend fun getAreaAttendance(
        areaId: Long,
        period: AttendancePeriod
    ): Result<AttendanceAreaReport> = coroutineScope {
        val dates = buildList {
            var current = period.from
            while (!current.isAfter(period.to)) {
                add(current)
                current = current.plusDays(1)
            }
        }
        val requests = dates.map { date ->
            async {
                apiCall { attendanceService.getAreaAttendance(areaId, date.toString()) }
                    .getOrElse { throw it }
                    .map(AttendanceMapper::toDomain)
            }
        }

        runCatching {
            val records = requests.awaitAll().flatten().distinctBy { it.id ?: "${it.employeeId}-${it.workDate}" }
            val areaName = records.firstOrNull()?.areaName ?: "Área"
            val countedDays = records.size
            val punctual = records.count { it.status == AttendanceStatus.ON_TIME }
            AttendanceAreaReport(
                areaId = areaId,
                areaName = areaName,
                punctualityPercentage = if (countedDays == 0) 0 else ((punctual * 100.0) / countedDays).roundToInt(),
                lateCount = records.count { it.status == AttendanceStatus.LATE },
                absenceCount = records.count { it.status == AttendanceStatus.ABSENT },
                totalEffectiveHours = records.sumOf { it.effectiveHours ?: 0.0 },
                totalOvertimeHours = records.sumOf { it.overtimeHours ?: 0.0 },
                employees = records
                    .groupBy { it.employeeId }
                    .map { (employeeId, employeeRecords) ->
                        AttendanceEmployeeSummary(
                            employeeId = employeeId,
                            employeeName = employeeRecords.firstNotNullOfOrNull { it.employeeName } ?: "Colaborador #$employeeId",
                            effectiveHours = employeeRecords.sumOf { it.effectiveHours ?: 0.0 },
                            overtimeHours = employeeRecords.sumOf { it.overtimeHours ?: 0.0 },
                            lateCount = employeeRecords.count { it.status == AttendanceStatus.LATE },
                            absenceCount = employeeRecords.count { it.status == AttendanceStatus.ABSENT },
                            status = employeeRecords.lastOrNull()?.status ?: AttendanceStatus.INCOMPLETE
                        )
                    }
                    .sortedBy { it.employeeName },
                records = records
            )
        }
    }

    override suspend fun getHoursReport(
        period: AttendancePeriod,
        areaId: Long?,
        orderByOvertime: Boolean
    ): Result<AttendanceHoursReport> {
        val resolvedAreaId = areaId ?: return Result.failure(IllegalArgumentException("Selecciona un área."))
        return getAreaAttendance(resolvedAreaId, period).map { report ->
            val employees = report.records
                .groupBy { it.employeeId }
                .map { (employeeId, records) ->
                    EmployeeHoursSummary(
                        employeeId = employeeId,
                        employeeName = records.firstNotNullOfOrNull { it.employeeName } ?: "Colaborador #$employeeId",
                        effectiveHours = records.sumOf { it.effectiveHours ?: 0.0 },
                        overtimeHours = records.sumOf { it.overtimeHours ?: 0.0 }
                    )
                }
                .let { values -> if (orderByOvertime) values.sortedByDescending { it.overtimeHours } else values.sortedBy { it.employeeName } }

            AttendanceHoursReport(
                totalEffectiveHours = employees.sumOf { it.effectiveHours },
                totalOvertimeHours = employees.sumOf { it.overtimeHours },
                employees = employees
            )
        }
    }

    override suspend fun registerPunch(type: PunchType): Result<AttendanceRecord?> =
        apiCall {
            attendanceService.registerPunch(
                PunchRequestDto(
                    employeeId = currentEmployeeProvider.currentEmployeeId().value,
                    type = type.name,
                    punchedAt = Instant.now().toString()
                )
            )
        }.mapCatching(AttendanceMapper::toDomain)

    override suspend fun justifyAttendance(
        attendanceRecordId: Long,
        reason: String,
        evidenceUrl: String?
    ): Result<AttendanceRecord> =
        apiCall {
            attendanceService.justifyAttendance(
                attendanceRecordId,
                JustifyAttendanceRequestDto(reason = reason.trim(), evidenceUrl = evidenceUrl)
            )
        }.mapCatching(AttendanceMapper::toDomain)

    override suspend fun getAreas(): Result<List<AttendanceArea>> =
        apiCall { workspaceService.getAreas() }.mapCatching { it.map(AttendanceMapper::toArea) }

    override suspend fun searchEmployees(query: String): Result<List<AttendanceEmployee>> =
        apiCall { workspaceService.searchEmployees(query.takeIf { it.isNotBlank() }) }
            .mapCatching { it.map(AttendanceMapper::toEmployee) }

    private fun Double.roundToInt(): Int = round(this).toInt()
}
