package com.performily.flowboard.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.MyProfileRoute
import com.performily.flowboard.features.attendance.presentation.ui.navigation.attendanceNavGraph
import com.performily.flowboard.features.workspace.presentation.ui.navigation.OrganizationRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.workspaceNavGraph

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: Any,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        workspaceNavGraph(navController)
        attendanceNavGraph(navController)

        composable<MoreRoute> {
            MoreScreen(
                onOrganizationClick = { navController.navigate(OrganizationRoute) },
                onMyProfileClick = { navController.navigate(MyProfileRoute) },
                onPendingClick = { title -> navController.navigate(PendingFeatureRoute(title)) }
            )
        }

        composable<PendingFeatureRoute> { backStackEntry ->
            PendingFeatureScreen(
                title = backStackEntry.toRoute<PendingFeatureRoute>().title,
                onBack = { navController.popBackStack() }
            )
        }

        // PENDIENTE: reemplazar por el NavGraph de cada bounded context al integrarlo.
        composable<PanelRoute> { PendingFeatureScreen(title = "Panel") }
        composable<HomeRoute> { PendingFeatureScreen(title = "Inicio") }
        composable<RequestsRoute> { PendingFeatureScreen(title = "Solicitudes") }
        composable<AttendanceRoute> { PendingFeatureScreen(title = "Asistencia") }
        composable<PayslipsRoute> { PendingFeatureScreen(title = "Boletas") }
    }
}
