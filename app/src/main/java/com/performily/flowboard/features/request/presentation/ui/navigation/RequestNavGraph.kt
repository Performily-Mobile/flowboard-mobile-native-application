package com.performily.flowboard.features.request.presentation.ui.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.performily.flowboard.core.navigation.RequestsRoute
import com.performily.flowboard.features.request.presentation.ui.screens.NewRequestScreen
import com.performily.flowboard.features.request.presentation.ui.screens.NewRequestTypeScreen
import com.performily.flowboard.features.request.presentation.ui.screens.RequestDetailScreen
import com.performily.flowboard.features.request.presentation.ui.screens.RequestTypesScreen
import com.performily.flowboard.features.request.presentation.ui.screens.RequestsScreen
import com.performily.flowboard.features.request.presentation.ui.screens.ResolvedRequestsScreen
import com.performily.flowboard.features.request.presentation.ui.screens.ReviewRequestScreen
import kotlinx.serialization.Serializable


@Serializable
data object NewRequestRoute


@Serializable
data class RequestDetailRoute(val requestId: Long)


@Serializable
data class ReviewRequestRoute(val requestId: Long)


@Serializable
data object ResolvedRequestsRoute


@Serializable
data object RequestTypesRoute


@Serializable
data object NewRequestTypeRoute


private const val REQUEST_RESULT_KEY = "request_result"


fun NavGraphBuilder.requestNavGraph(navController: NavController) {

    /** Vuelve a la pantalla anterior dejándole un mensaje para su snackbar. */
    fun popWithResult(message: String) {
        navController.previousBackStackEntry?.savedStateHandle?.set(REQUEST_RESULT_KEY, message)
        navController.popBackStack()
    }

    composable<RequestsRoute> { backStackEntry ->
        val resultMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(REQUEST_RESULT_KEY, null)
            .collectAsStateWithLifecycle()
        RequestsScreen(
            onNewRequest = { navController.navigate(NewRequestRoute) },
            onMyRequestClick = { requestId -> navController.navigate(RequestDetailRoute(requestId)) },
            onReviewClick = { requestId -> navController.navigate(ReviewRequestRoute(requestId)) },
            onResolvedClick = { navController.navigate(ResolvedRequestsRoute) },
            resultMessage = resultMessage,
            onResultMessageConsumed = { backStackEntry.savedStateHandle[REQUEST_RESULT_KEY] = null }
        )
    }

    composable<NewRequestRoute> {
        NewRequestScreen(
            onClose = { navController.popBackStack() },
            onSubmitted = ::popWithResult
        )
    }

    composable<RequestDetailRoute> { backStackEntry ->
        RequestDetailScreen(
            requestId = backStackEntry.toRoute<RequestDetailRoute>().requestId,
            onBack = { navController.popBackStack() },
            onFinished = ::popWithResult
        )
    }

    composable<ReviewRequestRoute> { backStackEntry ->
        ReviewRequestScreen(
            requestId = backStackEntry.toRoute<ReviewRequestRoute>().requestId,
            onBack = { navController.popBackStack() },
            onResolved = ::popWithResult
        )
    }

    composable<ResolvedRequestsRoute> {
        ResolvedRequestsScreen(
            onBack = { navController.popBackStack() },
            onRequestClick = { requestId -> navController.navigate(ReviewRequestRoute(requestId)) }
        )
    }

    composable<RequestTypesRoute> { backStackEntry ->
        val resultMessage by backStackEntry.savedStateHandle
            .getStateFlow<String?>(REQUEST_RESULT_KEY, null)
            .collectAsStateWithLifecycle()
        RequestTypesScreen(
            onBack = { navController.popBackStack() },
            onNewType = { navController.navigate(NewRequestTypeRoute) },
            resultMessage = resultMessage,
            onResultMessageConsumed = { backStackEntry.savedStateHandle[REQUEST_RESULT_KEY] = null }
        )
    }

    composable<NewRequestTypeRoute> {
        NewRequestTypeScreen(
            onClose = { navController.popBackStack() },
            onCreated = ::popWithResult
        )
    }
}
