package com.performily.flowboard.features.iam.presentation.ui.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.performily.flowboard.features.iam.presentation.ui.screens.AccountSecurityScreen
import com.performily.flowboard.features.iam.presentation.ui.screens.LegalScreen
import kotlinx.serialization.Serializable

@Serializable data object AccountSecurityRoute
fun NavGraphBuilder.iamNavGraph(navController: NavHostController) {
    composable<AccountSecurityRoute> { AccountSecurityScreen(onBack = { navController.popBackStack() }) }
}
