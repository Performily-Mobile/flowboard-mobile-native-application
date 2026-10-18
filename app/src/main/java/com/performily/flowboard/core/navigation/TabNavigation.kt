package com.performily.flowboard.core.navigation

import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination

/**
 * Opens a tab of the bottom bar, always on its first screen.
 *
 * The screens opened inside the previous tab are discarded instead of being saved and restored.
 * Restoring them made a tab reopen on an inner screen (a report, a detail, resolved requests),
 * so the user could not get back to the tab's main list.
 *
 * The start tab (the dashboard for HR, the home for employees) always sits at the bottom of the
 * stack, so it is reached by popping back to it.
 *
 * @param route route of the tab to open.
 */
fun NavController.navigateToTab(route: Any) {
    val start = graph.findStartDestination()
    if (start.hasRoute(route::class)) {
        popBackStack(start.id, inclusive = false)
        return
    }
    navigate(route) {
        popUpTo(start.id) { inclusive = false }
        launchSingleTop = true
    }
}
