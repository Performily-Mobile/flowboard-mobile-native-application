package com.performily.flowboard.features.workspace.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.workspace.presentation.ui.screens.EmployeeDetailScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.EmployeesScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.OrganizationChartScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.OrganizationScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.RegisterEmployeeScreen
import kotlinx.serialization.Serializable

@Serializable
data object WorkspaceNavGraphRoute

@Serializable
data object EmployeesRoute

@Serializable
data object RegisterEmployeeRoute

@Serializable
data class EmployeeDetailRoute(val employeeId: Long)

@Serializable
data object OrganizationRoute

@Serializable
data object OrganizationChartRoute

fun NavGraphBuilder.workspaceNavGraph(navController: NavController) {

    navigation<WorkspaceNavGraphRoute>(startDestination = EmployeesRoute) {

        composable<EmployeesRoute> {
            EmployeesScreen(
                onEmployeeClick = { employeeId -> navController.navigate(EmployeeDetailRoute(employeeId)) },
                onRegisterClick = { navController.navigate(RegisterEmployeeRoute) },
                onOrganizationClick = { navController.navigate(OrganizationRoute) },
                onOrganizationChartClick = { navController.navigate(OrganizationChartRoute) }
            )
        }

        composable<RegisterEmployeeRoute> {
            RegisterEmployeeScreen(
                onClose = { navController.popBackStack() },
                onRegistered = { employeeId ->
                    navController.navigate(EmployeeDetailRoute(employeeId)) {
                        popUpTo<RegisterEmployeeRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<EmployeeDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<EmployeeDetailRoute>()
            EmployeeDetailScreen(
                employeeId = route.employeeId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<OrganizationRoute> {
            OrganizationScreen(onBack = { navController.popBackStack() })
        }

        composable<OrganizationChartRoute> {
            OrganizationChartScreen(onBack = { navController.popBackStack() })
        }
    }
}
