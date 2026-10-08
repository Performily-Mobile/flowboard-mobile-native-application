package com.performily.flowboard.features.benefits.presentation.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.benefits.presentation.ui.screens.AssignBenefitScreen
import com.performily.flowboard.features.benefits.presentation.ui.screens.BenefitsAdminScreen
import com.performily.flowboard.features.benefits.presentation.ui.screens.MyBenefitsScreen
import com.performily.flowboard.features.benefits.presentation.ui.screens.MyVacationBalanceScreen
import com.performily.flowboard.features.benefits.presentation.ui.screens.VacationBalanceDetailScreen
import com.performily.flowboard.features.benefits.presentation.ui.screens.VacationBalancesScreen
import kotlinx.serialization.Serializable

@Serializable
data object BenefitsNavGraphRoute

/** MA-60 / MA-62 · Beneficios de RR.HH. */
@Serializable
data object BenefitsAdminRoute

/** MA-61 */
@Serializable
data object AssignBenefitRoute

/** Lista de saldos para RR.HH. */
@Serializable
data object VacationBalancesRoute

/** MA-63 */
@Serializable
data class VacationBalanceDetailRoute(val employeeId: Long, val employeeName: String)

/** MA-58 / MA-59 · Vista del colaborador. */
@Serializable
data object MyBenefitsRoute

/** MA-57 · Vista del colaborador. */
@Serializable
data object MyVacationBalanceRoute

/** Clave con la que "Asignar beneficio" deja su mensaje a la pantalla anterior. */
private const val ASSIGNMENT_RESULT_KEY = "benefits_assignment_result"

/**
 * @param onRequestVacation "Solicitar vacaciones" de MA-57 lleva al bounded context Request
 */
fun NavGraphBuilder.benefitsNavGraph(
    navController: NavController,
    onRequestVacation: () -> Unit
) {

    navigation<BenefitsNavGraphRoute>(startDestination = BenefitsAdminRoute) {

        composable<BenefitsAdminRoute> { backStackEntry ->
            val resultMessage by backStackEntry.savedStateHandle
                .getStateFlow<String?>(ASSIGNMENT_RESULT_KEY, null)
                .collectAsStateWithLifecycle()
            BenefitsAdminScreen(
                onBack = { navController.popBackStack() },
                onAssignClick = { navController.navigate(AssignBenefitRoute) },
                onVacationBalancesClick = { navController.navigate(VacationBalancesRoute) },
                resultMessage = resultMessage,
                onResultMessageConsumed = { backStackEntry.savedStateHandle[ASSIGNMENT_RESULT_KEY] = null }
            )
        }

        composable<AssignBenefitRoute> {
            AssignBenefitScreen(
                onClose = { navController.popBackStack() },
                onAssigned = { message ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(ASSIGNMENT_RESULT_KEY, message)
                    navController.popBackStack()
                }
            )
        }

        composable<VacationBalancesRoute> {
            VacationBalancesScreen(
                onBack = { navController.popBackStack() },
                onEmployeeClick = { employeeId, employeeName ->
                    navController.navigate(VacationBalanceDetailRoute(employeeId, employeeName))
                }
            )
        }

        composable<VacationBalanceDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VacationBalanceDetailRoute>()
            VacationBalanceDetailScreen(
                employeeId = route.employeeId,
                employeeName = route.employeeName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<MyBenefitsRoute> {
            MyBenefitsScreen(onBack = { navController.popBackStack() })
        }

        composable<MyVacationBalanceRoute> {
            MyVacationBalanceScreen(
                onBack = { navController.popBackStack() },
                onRequestVacation = onRequestVacation
            )
        }
    }
}
