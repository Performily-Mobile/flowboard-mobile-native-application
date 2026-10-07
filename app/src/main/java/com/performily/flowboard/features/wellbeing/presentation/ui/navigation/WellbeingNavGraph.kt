package com.performily.flowboard.features.wellbeing.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.screens.NewOfficeScreen
import com.performily.flowboard.features.wellbeing.presentation.ui.screens.OfficeDetailScreen
import com.performily.flowboard.features.wellbeing.presentation.ui.screens.OfficesScreen
import com.performily.flowboard.features.wellbeing.presentation.ui.screens.ReadingHistoryScreen
import com.performily.flowboard.features.wellbeing.presentation.ui.screens.ThresholdsScreen
import kotlinx.serialization.Serializable

@Serializable
data object WellbeingNavGraphRoute

/** MA-70 */
@Serializable
data object OfficesRoute

/** MA-71 */
@Serializable
data object NewOfficeRoute

/** MA-72, MA-73, MA-81 */
@Serializable
data class OfficeDetailRoute(val officeId: Long)

/** MA-74 */
@Serializable
data class ThresholdsRoute(val officeId: Long, val officeName: String)

/** MA-75 */
@Serializable
data class ReadingHistoryRoute(val officeId: Long, val officeName: String)

fun NavGraphBuilder.wellbeingNavGraph(navController: NavController) {

    navigation<WellbeingNavGraphRoute>(startDestination = OfficesRoute) {

        composable<OfficesRoute> {
            OfficesScreen(
                onBack = { navController.popBackStack() },
                onOfficeClick = { officeId -> navController.navigate(OfficeDetailRoute(officeId)) },
                onNewOfficeClick = { navController.navigate(NewOfficeRoute) }
            )
        }

        composable<NewOfficeRoute> {
            NewOfficeScreen(
                onClose = { navController.popBackStack() },
                onCreated = { officeId ->
                    // Al crear se pasa al detalle para vincular dispositivos; "atrás" vuelve a la lista.
                    navController.navigate(OfficeDetailRoute(officeId)) {
                        popUpTo<NewOfficeRoute> { inclusive = true }
                    }
                }
            )
        }

        composable<OfficeDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<OfficeDetailRoute>()
            OfficeDetailScreen(
                officeId = route.officeId,
                onBack = { navController.popBackStack() },
                onThresholdsClick = { officeId, officeName ->
                    navController.navigate(ThresholdsRoute(officeId, officeName))
                },
                onHistoryClick = { officeId, officeName ->
                    navController.navigate(ReadingHistoryRoute(officeId, officeName))
                }
            )
        }

        composable<ThresholdsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ThresholdsRoute>()
            ThresholdsScreen(
                officeId = route.officeId,
                officeName = route.officeName,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ReadingHistoryRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReadingHistoryRoute>()
            ReadingHistoryScreen(
                officeId = route.officeId,
                officeName = route.officeName,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
