package com.performily.flowboard.features.dashboard.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.performily.flowboard.core.navigation.AttendanceRoute
import com.performily.flowboard.core.navigation.HomeRoute
import com.performily.flowboard.core.navigation.PanelRoute
import com.performily.flowboard.core.navigation.navigateToTab
import com.performily.flowboard.core.navigation.RequestsRoute
import com.performily.flowboard.features.attendance.presentation.ui.navigation.AttendanceAreaReportRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyBenefitsRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyVacationBalanceRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.VacationBalancesRoute
import com.performily.flowboard.features.dashboard.presentation.ui.screens.EmployeeDashboardScreen
import com.performily.flowboard.features.dashboard.presentation.ui.screens.HrDashboardScreen
import com.performily.flowboard.features.notifications.presentation.ui.navigation.NotificationsRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestDetailRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.ReviewRequestRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EmployeesRoute

/**
 * Registers the HR "Panel" tab (MA-18) and the employee "Inicio" tab (MA-14).
 *
 * Cards that match a tab (Personal, Solicitudes, Asistencia) switch tabs; reports and details open
 * on top of the dashboard so that "back" returns to it.
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
            onNotificationsClick = { navController.navigate(NotificationsRoute) }
        )
    }

    composable<HomeRoute> {
        EmployeeDashboardScreen(
            onAttendanceClick = { navController.navigateToTab(AttendanceRoute) },
            onRequestsClick = { navController.navigateToTab(RequestsRoute) },
            onRequestClick = { requestId -> navController.navigate(RequestDetailRoute(requestId)) },
            onVacationClick = { navController.navigate(MyVacationBalanceRoute) },
            onBenefitsClick = { navController.navigate(MyBenefitsRoute) },
            onNotificationsClick = { navController.navigate(NotificationsRoute) }
        )
    }
}
