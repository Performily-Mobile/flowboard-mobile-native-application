package com.performily.flowboard.features.payroll.presentation.ui.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.toRoute
import com.performily.flowboard.core.navigation.PayslipsRoute
import com.performily.flowboard.features.payroll.presentation.ui.screens.MyPayslipsScreen
import com.performily.flowboard.features.payroll.presentation.ui.screens.PaymentStatusScreen
import com.performily.flowboard.features.payroll.presentation.ui.screens.PayslipViewerScreen
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
 * Payslip viewer of the employee (MA-66).
 *
 * @param payslipId payslip to show.
 */
@Serializable
data class PayslipViewerRoute(val payslipId: Long)

/**
 * Registers the payroll screens: payslip upload and payment status for HR, and the "Boletas" tab
 * (MA-65) with its viewer (MA-66) for employees.
 *
 * The protected access screen of the employee (ProtectedPayslipsRoute MA-64) is still pending.
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

    composable<PayslipsRoute> {
        MyPayslipsScreen(
            onPayslipClick = { payslipId -> navController.navigate(PayslipViewerRoute(payslipId)) }
        )
    }

    composable<PayslipViewerRoute> { backStackEntry ->
        PayslipViewerScreen(
            payslipId = backStackEntry.toRoute<PayslipViewerRoute>().payslipId,
            onBack = { navController.popBackStack() }
        )
    }
}
