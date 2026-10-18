package com.performily.flowboard.features.payroll.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.features.payroll.presentation.ui.screens.PaymentStatusScreen
import com.performily.flowboard.features.payroll.presentation.ui.screens.UploadPayslipsScreen
import kotlinx.serialization.Serializable

/** Route of the payroll graph. */
@Serializable
data object PayrollNavGraphRoute

/** Payslips and payments screen for HR (MA-67, MA-68). */
@Serializable
data object UploadPayslipsRoute

/**
 * Payment status screen for HR (MA-69).
 *
 * @param payrollPeriodId payroll period that was selected in MA-67, or null.
 */
@Serializable
data class PaymentStatusRoute(val payrollPeriodId: Long? = null)

/**
 * Registers the payroll screens for HR: payslip upload and payment status.
 *
 * The employee screens (ProtectedPayslipsRoute MA-64, MyPayslipsRoute MA-65 and
 * PayslipViewerRoute MA-66) are still pending and will be added to this same graph.
 *
 * @param navController controller used to move between the screens.
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
