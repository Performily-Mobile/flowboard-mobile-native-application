package com.performily.flowboard.features.attendance.infrastructure.repository

import com.performily.flowboard.core.network.apiCall
import com.performily.flowboard.core.session.CurrentEmployeeProvider
import com.performily.flowboard.features.attendance.domain.repository.AttendancePage
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
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import kotlin.math.round

class AttendanceRepositoryImpl @Inject constructor(
    private val attendanceService: AttendanceService,
    private val workspaceService: AttendanceWorkspaceService,
    private val currentEmployeeProvider: CurrentEmployeeProvider
) : AttendanceRepository {

    override suspend fun getMyAttendance(period: AttendancePeriod): Result<List<AttendanceRecord>> {
        val employeeId = currentEmployeeProvider.currentEmployeeId().value
        val recordsResult = apiCall {
            attendanceService.getMyAttendance(
                fromDate = period.from.toString(),
                toDate = period.to.toString()
            )
        }.mapCatching { it.map(AttendanceMapper::toDomain) }

        if (recordsResult.isFailure) return recordsResult

        return apiCall { workspaceService.getEmployee(employeeId) }
            .mapCatching { employee ->
                val employeeName = employee.fullName
                    ?: listOf(employee.firstName, employee.lastName).joinToString(" ").trim()
                recordsResult.getOrThrow().map { record ->
                    record.copy(
                        employeeName = employeeName.ifBlank { record.employeeName },
                        areaId = record.areaId ?: employee.areaId,
                        areaName = record.areaName ?: employee.areaName,
                        positionTitle = record.positionTitle ?: employee.positionTitle
                    )
                }
            }
            .recoverCatching { recordsResult.getOrThrow() }
    }

    override suspend fun getEmployeeAttendance(
        employeeId: Long,
        period: AttendancePeriod
    ): Result<List<AttendanceRecord>> {
        val recordsResult = apiCall {
            attendanceService.getEmployeeAttendance(
                employeeId = employeeId,
                fromDate = period.from.toString(),
                toDate = period.to.toString()
            )
        }.mapCatching { it.map(AttendanceMapper::toDomain) }

        if (recordsResult.isFailure) return recordsResult

        return apiCall { workspaceService.getEmployee(employeeId) }
            .mapCatching { employee ->
                val employeeName = employee.fullName
                    ?: listOf(employee.firstName, employee.lastName).joinToString(" ").trim()
                recordsResult.getOrThrow().map { record ->
                    record.copy(
                        employeeName = employeeName.ifBlank { record.employeeName },
                        areaId = record.areaId ?: employee.areaId,
                        areaName = record.areaName ?: employee.areaName,
                        positionTitle = record.positionTitle ?: employee.positionTitle
                    )
                }
            }
            .recoverCatching { recordsResult.getOrThrow() }
    }

    override suspend fun getRecords(
        period: AttendancePeriod, status: String?, page: Int, size: Int
    ): Result<AttendancePage> = runCatching {
        val response = apiCall {
            attendanceService.getRecords(
                period.from.toString(), period.to.toString(), status, page, size
            )
        }.getOrThrow()
        // Fetch the employee directory once per page instead of one API call per record.
        val employees = apiCall {
            workspaceService.searchEmployees(search = null, status = null)
        }.getOrNull().orEmpty().associateBy { it.id }
        val records = response.content.map(AttendanceMapper::toDomain).map { record ->
            val employee = employees[record.employeeId]
            record.copy(
                employeeName = employee?.fullName
                    ?: listOfNotNull(employee?.firstName, employee?.lastName).joinToString(" ").ifBlank { record.employeeName },
                areaId = record.areaId ?: employee?.areaId,
                areaName = record.areaName ?: employee?.areaName,
                positionTitle = record.positionTitle ?: employee?.positionTitle
            )
        }
        AttendancePage(records, response.totalElements, response.last)
    }

    override suspend fun getAreaAttendance(
        areaId: Long, period: AttendancePeriod
    ): Result<AttendanceAreaReport> = coroutineScope {
        runCatching {
            val summary = apiCall {
                attendanceService.getAreaSummary(areaId, period.from.toString(), period.to.toString())
            }.getOrThrow()
            val rawRecords = apiCall {
                attendanceService.getAreaRecords(areaId, period.from.toString(), period.to.toString())
            }.getOrThrow().map(AttendanceMapper::toDomain)
            val employees = apiCall {
                workspaceService.searchEmployees(search = null, areaId = areaId, status = "ACTIVE")
            }.getOrThrow()
            val grouped = rawRecords.groupBy { it.employeeId }
            val records = rawRecords.map { record ->
                val employee = employees.firstOrNull { it.id == record.employeeId }
                record.copy(
                    employeeName = employee?.fullName
                        ?: listOfNotNull(employee?.firstName, employee?.lastName).joinToString(" ").ifBlank { record.employeeName },
                    areaId = record.areaId ?: areaId,
                    areaName = record.areaName ?: employee?.areaName,
                    positionTitle = record.positionTitle ?: employee?.positionTitle
                )
            }
            val count = summary.onTime + summary.late + summary.absent + summary.incomplete + summary.justified
            val punctuality = if (count == 0L) 0 else round(summary.onTime * 100.0 / count).toInt()
            AttendanceAreaReport(
                areaId = areaId,
                areaName = employees.firstOrNull()?.areaName ?: "Área $areaId",
                punctualityPercentage = punctuality,
                lateCount = summary.late.toInt(),
                absenceCount = summary.absent.toInt(),
                totalEffectiveHours = summary.workedHours,
                totalOvertimeHours = summary.overtimeHours,
                employees = employees.map { employee ->
                    val rows = grouped[employee.id].orEmpty()
                    AttendanceEmployeeSummary(
                        employeeId = employee.id,
                        employeeName = employee.fullName
                            ?: listOfNotNull(employee.firstName, employee.lastName).joinToString(" ").ifBlank { "Colaborador #${employee.id}" },
                        effectiveHours = rows.sumOf { it.effectiveHours ?: 0.0 },
                        overtimeHours = rows.sumOf { it.overtimeHours ?: 0.0 },
                        lateCount = rows.count { it.status == AttendanceStatus.LATE },
                        absenceCount = rows.count { it.status == AttendanceStatus.ABSENT },
                        status = rows.maxByOrNull { it.workDate }?.status ?: AttendanceStatus.INCOMPLETE,
                        hasRecords = rows.isNotEmpty()
                    )
                }.sortedBy { it.employeeName },
                records = records
            )
        }
    }

    override suspend fun getHoursReport(
        period: AttendancePeriod,
        areaId: Long?,
        orderByOvertime: Boolean
    ): Result<AttendanceHoursReport> = coroutineScope {
        val resolvedAreaId = areaId ?: return@coroutineScope Result.failure(
            IllegalArgumentException("Selecciona un área.")
        )

        val employeesResult = apiCall {
            workspaceService.searchEmployees(
                search = null,
                areaId = resolvedAreaId,
                status = "ACTIVE"
            )
        }

        if (employeesResult.isFailure) {
            return@coroutineScope Result.failure(
                employeesResult.exceptionOrNull() ?: IllegalStateException("No se pudieron cargar los colaboradores del área.")
            )
        }

        val employees = employeesResult.getOrThrow()
        val summaries = employees.map { employee ->
            async {
                employee.id to apiCall {
                    attendanceService.getEmployeeHours(
                        employeeId = employee.id,
                        fromDate = period.from.toString(),
                        toDate = period.to.toString()
                    )
                }.getOrElse { throw it }
            }
        }.awaitAll()

        val rows = summaries.map { (employeeId, summary) ->
            val employee = employees.first { it.id == employeeId }
            EmployeeHoursSummary(
                employeeId = employeeId,
                employeeName = employee.fullName
                    ?: listOfNotNull(employee.firstName, employee.lastName).joinToString(" ").ifBlank { "Colaborador #$employeeId" },
                effectiveHours = summary.workedHours,
                overtimeHours = summary.overtimeHours
            )
        }.let { values ->
            if (orderByOvertime) values.sortedByDescending { it.overtimeHours }
            else values.sortedBy { it.employeeName }
        }

        Result.success(
            AttendanceHoursReport(
                totalEffectiveHours = rows.sumOf { it.effectiveHours },
                totalOvertimeHours = rows.sumOf { it.overtimeHours },
                employees = rows
            )
        )
    }

    override suspend fun registerPunch(type: PunchType): Result<Long> =
        apiCall {
            attendanceService.registerPunch(
                PunchRequestDto(
                    employeeId = currentEmployeeProvider.currentEmployeeId().value,
                    type = type.name,
                    punchedAt = LocalDateTime.now(ZoneId.of("America/Lima")).toString()
                )
            )
        }

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
        apiCall { workspaceService.getAreas() }
            .mapCatching { it.filter { area -> area.active }.map(AttendanceMapper::toArea) }

    override suspend fun searchEmployees(query: String): Result<List<AttendanceEmployee>> =
        apiCall { workspaceService.searchEmployees(query.takeIf { it.isNotBlank() }) }
            .mapCatching { it.map(AttendanceMapper::toEmployee) }
}
