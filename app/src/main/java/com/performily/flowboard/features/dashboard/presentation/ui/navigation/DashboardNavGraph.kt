package com.performily.flowboard.features.dashboard.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.performily.flowboard.core.navigation.PanelRoute
import com.performily.flowboard.core.navigation.PendingFeatureRoute
import com.performily.flowboard.core.navigation.RequestsRoute
import com.performily.flowboard.features.attendance.presentation.ui.navigation.AttendanceAreaReportRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.VacationBalancesRoute
import com.performily.flowboard.features.dashboard.presentation.ui.screens.HrDashboardScreen
import com.performily.flowboard.features.request.presentation.ui.navigation.ReviewRequestRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EmployeesRoute

/**
 * Registers the HR "Panel" tab (MA-18).
 *
 * Cards that match a tab (Personal, Solicitudes) switch tabs; reports open on top of the
 * dashboard so that "back" returns to it.
 */
fun NavGraphBuilder.dashboardNavGraph(navController: NavController) {
    composable<PanelRoute> {
        HrDashboardScreen(
            onActiveEmployeesClick = { navController.navigateToTab(EmployeesRoute) },
            onPendingRequestsClick = { navController.navigateToTab(RequestsRoute) },
            onLatenessClick = { navController.navigate(AttendanceAreaReportRoute) },
            onExpiringVacationsClick = { navController.navigate(VacationBalancesRoute) },
            onAttendanceByAreaClick = { navController.navigate(AttendanceAreaReportRoute) },
            onRequestClick = { requestId -> navController.navigate(ReviewRequestRoute(requestId)) },
            onNotificationsClick = { navController.navigate(PendingFeatureRoute("Notificaciones")) }
        )
    }
}

/** Switches tab like the bottom bar does: without stacking screens and keeping each tab's state. */
private fun NavController.navigateToTab(route: Any) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
