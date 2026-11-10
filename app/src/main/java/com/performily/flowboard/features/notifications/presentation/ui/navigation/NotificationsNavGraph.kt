package com.performily.flowboard.features.notifications.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.performily.flowboard.core.navigation.RequestsRoute
import com.performily.flowboard.core.navigation.navigateToTab
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyBenefitsRoute
import com.performily.flowboard.features.notifications.domain.valueobject.NotificationTarget
import com.performily.flowboard.features.notifications.presentation.ui.screens.NotificationsScreen
import com.performily.flowboard.features.payroll.presentation.ui.navigation.PayslipViewerRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestDetailRoute
import kotlinx.serialization.Serializable

/** Notification screen, opened from the bell of the home screens and from "Más". */
@Serializable
data object NotificationsRoute

/**
 * Registers the notification screen and sends each notice to the screen of its source.
 *
 * Requests to approve switch to the "Solicitudes" tab; the rest open on top of the notifications so
 * that "back" returns to them.
 *
 * @param navController controller used to move between the screens.
 */
fun NavGraphBuilder.notificationsNavGraph(navController: NavController) {
    composable<NotificationsRoute> {
        NotificationsScreen(
            onBack = { navController.popBackStack() },
            onOpenTarget = { target ->
                when (target) {
                    is NotificationTarget.Request -> navController.navigate(RequestDetailRoute(target.requestId))
                    NotificationTarget.PendingApprovals -> navController.navigateToTab(RequestsRoute)
                    is NotificationTarget.Payslip -> navController.navigate(PayslipViewerRoute(target.payslipId))
                    NotificationTarget.MyBenefits -> navController.navigate(MyBenefitsRoute)
                }
            }
        )
    }
}
