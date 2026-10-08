package com.performily.flowboard.features.payroll.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.payroll.presentation.ui.screens.PaymentStatusScreen
import com.performily.flowboard.features.payroll.presentation.ui.screens.UploadPayslipsScreen
import kotlinx.serialization.Serializable

@Serializable
data object PayrollNavGraphRoute

/** MA-67 / MA-68 · Boletas y pagos (RR.HH.). */
@Serializable
data object UploadPayslipsRoute

/** MA-69 · Estado de pagos (RR.HH.). Abre con el período que estaba elegido en MA-67. */
@Serializable
data class PaymentStatusRoute(val payrollPeriodId: Long? = null)

/*
 * PENDIENTE (vista del colaborador): ProtectedPayslipsRoute (MA-64), MyPayslipsRoute (MA-65)
 * y PayslipViewerRoute (MA-66) se agregan a este mismo grafo.
 */
fun NavGraphBuilder.payrollNavGraph(navController: NavController) {

    navigation<PayrollNavGraphRoute>(startDestination = UploadPayslipsRoute) {

        composable<UploadPayslipsRoute> {
            UploadPayslipsScreen(
                onBack = { navController.popBackStack() },
                onViewPaymentStatus = { payrollPeriodId -> navController.navigate(PaymentStatusRoute(payrollPeriodId)) }
            )
        }

        composable<PaymentStatusRoute> { backStackEntry ->
            PaymentStatusScreen(
                initialPayrollPeriodId = backStackEntry.toRoute<PaymentStatusRoute>().payrollPeriodId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
