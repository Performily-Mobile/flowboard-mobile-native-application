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
import com.performily.flowboard.features.attendance.presentation.ui.navigation.JustifyAttendanceRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.NewRequestRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.NewRequestTypeRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestDetailRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestTypesRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.ReviewRequestRoute

/** Full-screen forms, where the bottom bar is hidden. */
private val routesWithoutBottomBar = listOf(
    RegisterEmployeeRoute::class,
    EditPersonalDataRoute::class,
    NewOfficeRoute::class,
    AssignBenefitRoute::class,
    UploadPayslipsRoute::class,
    PaymentStatusRoute::class,
    NewRequestRoute::class,
    RequestDetailRoute::class,
    ReviewRequestRoute::class,
    RequestTypesRoute::class,
    NewRequestTypeRoute::class,
    JustifyAttendanceRoute::class
)

/**
 * Root composable of the app: bottom bar by role plus the navigation host.
 *
 * Every screen handles its own system insets with its own Scaffold, so this one uses none.
 * HR opens on the dashboard and employees on their home.
 *
 * @param viewModel shell view model that exposes the role of the signed-in user.
 */
@Composable
fun FlowboardApp(viewModel: AppShellViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val destinations = remember(viewModel.role) { TopLevelDestination.forRole(viewModel.role) }
    val startDestination: Any = when (viewModel.role) {
        UserRole.HUMAN_RESOURCES -> PanelRoute
        UserRole.EMPLOYEE -> HomeRoute
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = currentDestination != null &&
            routesWithoutBottomBar.none { currentDestination.hasRoute(it) }

    Scaffold(
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

/**
 * Switches tab without stacking screens and keeping the state of each tab.
 *
 * The tab that is the start destination of the app (the dashboard for HR, the home for employees)
 * is reached by popping back to it, which always works because it sits at the bottom of the stack.
 * The other tabs are reached with a single-top navigation.
 */
private fun NavHostController.navigateToTopLevel(destination: TopLevelDestination) {
    val isStartTab = graph.findStartDestination().hasRoute(destination.route::class)
    if (isStartTab && popBackStack(destination.route, inclusive = false, saveState = true)) return

    navigate(destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
