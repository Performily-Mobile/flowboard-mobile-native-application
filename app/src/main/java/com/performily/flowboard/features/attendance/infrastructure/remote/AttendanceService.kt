package com.performily.flowboard.features.attendance.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceService {

    @GET("attendance/me")
    suspend fun getMyAttendance(
        @Query("employeeId") employeeId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<List<AttendanceRecordDto>>

    @GET("attendance/employees/{employeeId}")
    suspend fun getEmployeeAttendance(
        @Path("employeeId") employeeId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<List<AttendanceRecordDto>>

    @GET("attendance/areas/{areaId}")
    suspend fun getAreaAttendance(
        @Path("areaId") areaId: Long,
        @Query("workDate") workDate: String
    ): Response<List<AttendanceRecordDto>>

    @GET("attendance/reports/employees/{employeeId}/hours")
    suspend fun getEmployeeHours(
        @Path("employeeId") employeeId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<AttendanceHoursSummaryDto>

    @GET("attendance/reports/areas/{areaId}")
    suspend fun getAreaSummary(
        @Path("areaId") areaId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<AttendanceAreaSummaryDto>

    @POST("attendance/punches")
    suspend fun registerPunch(@Body request: PunchRequestDto): Response<Long>

    @POST("attendance/{attendanceRecordId}/justification")
    suspend fun justifyAttendance(
        @Path("attendanceRecordId") attendanceRecordId: Long,
        @Body request: JustifyAttendanceRequestDto
    ): Response<AttendanceRecordDto>
}
