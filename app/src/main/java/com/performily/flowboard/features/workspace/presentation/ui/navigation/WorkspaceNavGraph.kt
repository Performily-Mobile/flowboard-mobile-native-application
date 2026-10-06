package com.performily.flowboard.features.workspace.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.workspace.presentation.ui.screens.EditPersonalDataScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.EmployeeDetailScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.EmployeesScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.MyProfileScreen
import com.performily.flowboard.features.workspace.presentation.ui.screens.MyRecordScreen
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
data class EmployeeDetailRoute(val employeeId: Long, val openReassign: Boolean = false)

@Serializable
data class EditPersonalDataRoute(val employeeId: Long)

@Serializable
data object OrganizationRoute

@Serializable
data class OrganizationChartRoute(val areaId: Long? = null, val highlightedEmployeeId: Long? = null)

@Serializable
data object MyProfileRoute

@Serializable
data object MyRecordRoute

fun NavGraphBuilder.workspaceNavGraph(navController: NavController) {

    navigation<WorkspaceNavGraphRoute>(startDestination = EmployeesRoute) {

        composable<EmployeesRoute> {
            EmployeesScreen(
                onEmployeeClick = { employeeId -> navController.navigate(EmployeeDetailRoute(employeeId)) },
                onRegisterClick = { navController.navigate(RegisterEmployeeRoute) },
                onOrganizationClick = { navController.navigate(OrganizationRoute) },
                onOrganizationChartClick = { navController.navigate(OrganizationChartRoute()) },
                onMyProfileClick = { navController.navigate(MyProfileRoute) }
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
                openReassignOnStart = route.openReassign,
                onBack = { navController.popBackStack() },
                onEditPersonalData = { employeeId -> navController.navigate(EditPersonalDataRoute(employeeId)) },
                onOpenEmployee = { employeeId, openReassign ->
                    navController.navigate(EmployeeDetailRoute(employeeId, openReassign))
                }
            )
        }

        composable<EditPersonalDataRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<EditPersonalDataRoute>()
            EditPersonalDataScreen(
                employeeId = route.employeeId,
                onClose = { navController.popBackStack() }
            )
        }

        composable<OrganizationRoute> {
            OrganizationScreen(onBack = { navController.popBackStack() })
        }

        composable<OrganizationChartRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<OrganizationChartRoute>()
            OrganizationChartScreen(
                onBack = { navController.popBackStack() },
                areaId = route.areaId,
                highlightedEmployeeId = route.highlightedEmployeeId
            )
        }

        composable<MyProfileRoute> {
            MyProfileScreen(
                onBack = { navController.popBackStack() },
                onMyRecordClick = { navController.navigate(MyRecordRoute) },
                onOrganizationChartClick = { areaId, employeeId ->
                    navController.navigate(OrganizationChartRoute(areaId = areaId, highlightedEmployeeId = employeeId))
                }
            )
        }

        composable<MyRecordRoute> {
            MyRecordScreen(onBack = { navController.popBackStack() })
        }
    }
}
