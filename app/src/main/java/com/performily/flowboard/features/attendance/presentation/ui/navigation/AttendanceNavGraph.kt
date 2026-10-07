package com.performily.flowboard.features.attendance.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.core.navigation.AttendanceRoute
import com.performily.flowboard.features.attendance.presentation.ui.screens.AttendanceAreaScreen
import com.performily.flowboard.features.attendance.presentation.ui.screens.AttendanceEmployeeScreen
import com.performily.flowboard.features.attendance.presentation.ui.screens.AttendanceHomeScreen
import com.performily.flowboard.features.attendance.presentation.ui.screens.AttendanceHoursScreen
import com.performily.flowboard.features.attendance.presentation.ui.screens.JustifyAttendanceScreen
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data object AttendanceHomeRoute

@Serializable
data object AttendanceAreaReportRoute

@Serializable
data class AttendanceEmployeeRoute(
    val employeeId: Long,
    val employeeName: String = ""
)

@Serializable
data object AttendanceHoursRoute

@Serializable
data class JustifyAttendanceRoute(
    val attendanceRecordId: Long,
    val workDate: String
)

fun NavGraphBuilder.attendanceNavGraph(navController: NavController) {
    navigation<AttendanceRoute>(startDestination = AttendanceHomeRoute) {
        composable<AttendanceHomeRoute> {
            AttendanceHomeScreen(
                onAreaReport = { navController.navigate(AttendanceAreaReportRoute) },
                onEmployeeReport = {
                    // The employee report is normally opened from an area report row.
                    navController.navigate(AttendanceAreaReportRoute)
                },
                onHoursReport = { navController.navigate(AttendanceHoursRoute) },
                onJustify = { attendanceRecordId, workDate ->
                    navController.navigate(JustifyAttendanceRoute(attendanceRecordId, workDate.toString()))
                }
            )
        }

        composable<AttendanceAreaReportRoute> {
            AttendanceAreaScreen(
                onEmployeeClick = { employeeId, employeeName ->
                    navController.navigate(AttendanceEmployeeRoute(employeeId, employeeName))
                }
            )
        }

        composable<AttendanceEmployeeRoute> { entry ->
            val route = entry.toRoute<AttendanceEmployeeRoute>()
            AttendanceEmployeeScreen(
                employeeId = route.employeeId,
                employeeName = route.employeeName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<AttendanceHoursRoute> {
            AttendanceHoursScreen()
        }

        composable<JustifyAttendanceRoute> { entry ->
            val route = entry.toRoute<JustifyAttendanceRoute>()
            JustifyAttendanceScreen(
                attendanceRecordId = route.attendanceRecordId,
                workDate = LocalDate.parse(route.workDate),
                onClose = { navController.popBackStack() }
            )
        }
    }
}
