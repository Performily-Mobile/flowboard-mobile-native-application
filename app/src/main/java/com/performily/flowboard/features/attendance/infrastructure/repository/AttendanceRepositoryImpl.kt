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
import java.time.Instant
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

        // AttendanceRecordResource deliberately exposes only attendance data. Employee
        // identity/area data belongs to Workspace, so enrich the UI model through the
        // Workspace API without coupling Attendance to Workspace persistence.
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

    override suspend fun getAreaAttendance(
        areaId: Long,
        period: AttendancePeriod
    ): Result<AttendanceAreaReport> = coroutineScope {
        val summaryDeferred = async {
            apiCall {
                attendanceService.getAreaSummary(
                    areaId = areaId,
                    fromDate = period.from.toString(),
                    toDate = period.to.toString()
                )
            }
        }

        // The backend's area-report endpoint returns aggregate metrics only. The
        // mockup also needs the employee rows and record history, so retrieve the
        // daily area records for that presentation detail.
        val dates = generateSequence(period.from) { current ->
            current.plusDays(1).takeUnless { it.isAfter(period.to) }
        }.toList()

        val dailyRecordsDeferred = dates.map { date ->
            async {
                apiCall { attendanceService.getAreaAttendance(areaId, date.toString()) }
                    .getOrElse { throw it }
                    .map(AttendanceMapper::toDomain)
            }
        }

        runCatching {
            val summary = summaryDeferred.await().getOrElse { throw it }

            val rawRecords = dailyRecordsDeferred.awaitAll()
                .flatten()
                .distinctBy { it.id ?: "${it.employeeId}-${it.workDate}" }

            val employeeIds = rawRecords.map { it.employeeId }.distinct()
            val employeeMap = employeeIds.map { employeeId ->
                async {
                    employeeId to apiCall { workspaceService.getEmployee(employeeId) }
                        .getOrNull()
                }
            }.awaitAll().toMap()

            val records = rawRecords.map { record ->
                val employee = employeeMap[record.employeeId]
                record.copy(
                    employeeName = employee?.fullName
                        ?: listOfNotNull(employee?.firstName, employee?.lastName).joinToString(" ").ifBlank { record.employeeName },
                    areaId = record.areaId ?: employee?.areaId ?: areaId,
                    areaName = record.areaName ?: employee?.areaName,
                    positionTitle = record.positionTitle ?: employee?.positionTitle
                )
            }

            val totalStatusCount = summary.onTime + summary.late + summary.absent + summary.incomplete + summary.justified
            val punctuality = if (totalStatusCount == 0L) {
                0
            } else {
                round(summary.onTime * 100.0 / totalStatusCount).toInt()
            }

            AttendanceAreaReport(
                areaId = summary.areaId,
                areaName = records.firstOrNull()?.areaName ?: "Área $areaId",
                punctualityPercentage = punctuality,
                lateCount = summary.late.toInt(),
                absenceCount = summary.absent.toInt(),
                totalEffectiveHours = summary.workedHours,
                totalOvertimeHours = summary.overtimeHours,
                employees = records
                    .groupBy { it.employeeId }
                    .map { (employeeId, employeeRecords) ->
                        AttendanceEmployeeSummary(
                            employeeId = employeeId,
                            employeeName = employeeRecords.firstNotNullOfOrNull { it.employeeName }
                                ?: "Colaborador #$employeeId",
                            effectiveHours = employeeRecords.sumOf { it.effectiveHours ?: 0.0 },
                            overtimeHours = employeeRecords.sumOf { it.overtimeHours ?: 0.0 },
                            lateCount = employeeRecords.count { it.status == AttendanceStatus.LATE },
                            absenceCount = employeeRecords.count { it.status == AttendanceStatus.ABSENT },
                            status = employeeRecords.maxByOrNull { it.workDate }?.status ?: AttendanceStatus.INCOMPLETE
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
                    punchedAt = Instant.now().toString()
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

