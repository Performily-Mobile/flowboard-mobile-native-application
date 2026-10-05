package com.performily.flowboard.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.performily.flowboard.features.workspace.presentation.ui.navigation.WorkspaceNavGraphRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.workspaceNavGraph

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = WorkspaceNavGraphRoute
    ) {
        workspaceNavGraph(navController)
    }
}
