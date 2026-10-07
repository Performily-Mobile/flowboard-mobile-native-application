package com.performily.flowboard.core.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.payroll.presentation.ui.navigation.PaymentStatusRoute
import com.performily.flowboard.features.payroll.presentation.ui.navigation.UploadPayslipsRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.AssignBenefitRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.NewOfficeRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EditPersonalDataRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.RegisterEmployeeRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.WorkspaceNavGraphRoute

/** Formularios de pantalla completa: ahí no se muestra la barra inferior. */
private val routesWithoutBottomBar = listOf(
    RegisterEmployeeRoute::class,
    EditPersonalDataRoute::class,
    NewOfficeRoute::class,
    AssignBenefitRoute::class,
    // Payroll RR.HH.: MA-67 y MA-69 no muestran la barra inferior.
    UploadPayslipsRoute::class,
    PaymentStatusRoute::class,
    NewRequestRoute::class,
    RequestDetailRoute::class,
    ReviewRequestRoute::class,
    RequestTypesRoute::class,
    NewRequestTypeRoute::class
)

@Composable
fun FlowboardApp(viewModel: AppShellViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val destinations = remember(viewModel.role) { TopLevelDestination.forRole(viewModel.role) }
    val startDestination: Any = when (viewModel.role) {
        UserRole.HUMAN_RESOURCES -> WorkspaceNavGraphRoute
        UserRole.EMPLOYEE -> HomeRoute
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination != null &&
        routesWithoutBottomBar.none { currentDestination.hasRoute(it) }

    Scaffold(
        // Cada pantalla maneja sus propios márgenes del sistema con su Scaffold.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (showBottomBar) {
                FlowboardBottomBar(
                    destinations = destinations,
                    currentDestination = currentDestination,
                    onSelect = { navController.navigateToTopLevel(it) }
                )
            }
        }
    ) { paddingValues ->
        AppNavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        )
    }
}

/** Cambia de pestaña sin apilar pantallas y conservando el estado de cada una. */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
