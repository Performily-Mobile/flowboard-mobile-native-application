package com.performily.flowboard.features.dashboard.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Attendance summary per area, read from the Attendance backend. */
interface DashboardAttendanceService {

    /**
     * Returns the attendance counts of an area in a date range.
     *
     * @param areaId identifier of the area.
     * @param fromDate first day of the range in ISO format.
     * @param toDate last day of the range in ISO format.
     */
    @GET("attendance/reports/areas/{areaId}")
    suspend fun getAreaSummary(
        @Path("areaId") areaId: Long,
        @Query("from") fromDate: String,
        @Query("to") toDate: String
    ): Response<DashboardAttendanceSummaryDto>
}
