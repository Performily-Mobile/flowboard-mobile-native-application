package com.performily.flowboard.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.attendance.presentation.ui.navigation.MyAttendanceFromMoreRoute
import com.performily.flowboard.features.iam.presentation.ui.navigation.iamNavGraph
import com.performily.flowboard.features.iam.presentation.ui.navigation.AccountSecurityRoute
import com.performily.flowboard.features.payroll.presentation.ui.navigation.PayrollNavGraphRoute
import com.performily.flowboard.features.payroll.presentation.ui.navigation.payrollNavGraph
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.WellbeingNavGraphRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.BenefitsNavGraphRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyBenefitsRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyVacationBalanceRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.benefitsNavGraph
import com.performily.flowboard.features.notifications.presentation.ui.navigation.NotificationsRoute
import com.performily.flowboard.features.notifications.presentation.ui.navigation.notificationsNavGraph
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.wellbeingNavGraph
import com.performily.flowboard.features.workspace.presentation.ui.navigation.MyProfileRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.MyRecordRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.OrganizationChartRoute
import com.performily.flowboard.features.attendance.presentation.ui.navigation.attendanceNavGraph
import com.performily.flowboard.features.dashboard.presentation.ui.navigation.dashboardNavGraph
import com.performily.flowboard.features.workspace.presentation.ui.navigation.OrganizationRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.workspaceNavGraph
import com.performily.flowboard.features.request.presentation.ui.navigation.MyRequestsRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestTypesRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.requestNavGraph

/**
 * Root navigation host of the app.
 *
 * Each bounded context registers its own graph here; routes whose feature is not integrated yet
 * show a temporary screen.
 *
 * @param navController controller that drives the navigation.
 * @param startDestination route shown when the app opens, which depends on the user role.
 * @param role role of the signed-in user, used to build the "Más" screen.
 * @param modifier modifier applied to the host.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    role: UserRole,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        workspaceNavGraph(navController)
        payrollNavGraph(navController)
        wellbeingNavGraph(navController)
        benefitsNavGraph(navController, onRequestVacation = { navController.navigate(RequestsRoute) })
        requestNavGraph(navController)
        attendanceNavGraph(navController)
        dashboardNavGraph(navController)
        iamNavGraph(navController)
        notificationsNavGraph(navController)

        composable<MoreRoute> {
            MoreScreen(
                role = role,
                onOrganizationClick = { navController.navigate(OrganizationRoute) },
                onRequestTypesClick = { navController.navigate(RequestTypesRoute) },
                onBenefitsClick = { navController.navigate(BenefitsNavGraphRoute) },
                onPayslipsClick = { navController.navigate(PayrollNavGraphRoute) },
                onWellbeingClick = { navController.navigate(WellbeingNavGraphRoute) },
                onMyProfileClick = { navController.navigate(MyProfileRoute) },
                onMyRecordClick = { navController.navigate(MyRecordRoute) },
                onMyBenefitsClick = { navController.navigate(MyBenefitsRoute) },
                onMyVacationBalanceClick = { navController.navigate(MyVacationBalanceRoute) },
                onMyRequestsClick = { navController.navigate(MyRequestsRoute) },
                onMyAttendanceClick = { navController.navigate(MyAttendanceFromMoreRoute) },
                onOrganizationChartClick = { navController.navigate(OrganizationChartRoute()) },
                onNotificationsClick = { navController.navigate(NotificationsRoute) },
                onAccountSecurityClick = { navController.navigate(AccountSecurityRoute) }
            )
        }

        composable<PendingFeatureRoute> { backStackEntry ->
            PendingFeatureScreen(
                title = backStackEntry.toRoute<PendingFeatureRoute>().title,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
