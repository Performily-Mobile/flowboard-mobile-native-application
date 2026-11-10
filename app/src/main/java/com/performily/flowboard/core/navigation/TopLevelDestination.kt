package com.performily.flowboard.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.session.UserRole
import com.performily.flowboard.features.benefits.presentation.ui.navigation.AssignBenefitRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.BenefitsAdminRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyBenefitsRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.MyVacationBalanceRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.VacationBalanceDetailRoute
import com.performily.flowboard.features.benefits.presentation.ui.navigation.VacationBalancesRoute
import com.performily.flowboard.features.iam.presentation.ui.navigation.AccountSecurityRoute
import com.performily.flowboard.features.notifications.presentation.ui.navigation.NotificationsRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.NewOfficeRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.OfficeDetailRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.OfficesRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.ReadingHistoryRoute
import com.performily.flowboard.features.wellbeing.presentation.ui.navigation.ThresholdsRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EditPersonalDataRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EmployeeDetailRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.EmployeesRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.MyProfileRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.MyRecordRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.OrganizationChartRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.OrganizationRoute
import com.performily.flowboard.features.workspace.presentation.ui.navigation.RegisterEmployeeRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.NewRequestRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.NewRequestTypeRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestDetailRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.RequestTypesRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.ResolvedRequestsRoute
import com.performily.flowboard.features.request.presentation.ui.navigation.ReviewRequestRoute
import kotlin.reflect.KClass

/**
 * Tabs of the bottom bar.
 *
 * @property label text of the tab.
 * @property icon icon of the tab.
 * @property route screen the tab opens.
 * @property matches screens on which the tab is shown as selected.
 */
enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val matches: List<KClass<*>>
) {
    /** HR dashboard (MA-18). */
    PANEL("Panel", FlowboardIcons.GridView, PanelRoute, listOf(PanelRoute::class)),

    /** HR employee management. */
    PERSONAL(
        "Personal",
        FlowboardIcons.Group,
        EmployeesRoute,
        listOf(
            EmployeesRoute::class,
            EmployeeDetailRoute::class,
            RegisterEmployeeRoute::class,
            EditPersonalDataRoute::class
        )
    ),

    /** Requests, shared by both roles. */
    REQUESTS(
        "Solicitudes",
        FlowboardIcons.Article,
        RequestsRoute,
        listOf(
            RequestsRoute::class,
            NewRequestRoute::class,
            RequestDetailRoute::class,
            ReviewRequestRoute::class,
            ResolvedRequestsRoute::class
        )
    ),

    /** Attendance, shared by both roles. */
    ATTENDANCE(
        "Asistencia",
        FlowboardIcons.Schedule,
        AttendanceRoute,
        listOf(AttendanceRoute::class)
    ),

    /** Every other option, grouped by role in the "Más" screen (MA-19). */
    MORE(
        "Más",
        FlowboardIcons.Menu,
        MoreRoute,
        listOf(
            MoreRoute::class,
            OrganizationRoute::class,
            OrganizationChartRoute::class,
            MyProfileRoute::class,
            MyRecordRoute::class,
            PendingFeatureRoute::class,
            OfficesRoute::class,
            NewOfficeRoute::class,
            OfficeDetailRoute::class,
            ThresholdsRoute::class,
            ReadingHistoryRoute::class,
            BenefitsAdminRoute::class,
            AssignBenefitRoute::class,
            VacationBalancesRoute::class,
            VacationBalanceDetailRoute::class,
            MyBenefitsRoute::class,
            MyVacationBalanceRoute::class,
            RequestTypesRoute::class,
            NewRequestTypeRoute::class,
            NotificationsRoute::class,
            AccountSecurityRoute::class
        )
    ),

    /** Employee home (MA-14). */
    HOME("Inicio", FlowboardIcons.Home, HomeRoute, listOf(HomeRoute::class)),

    /** Own payslips of the employee (MA-65). */
    PAYSLIPS("Boletas", FlowboardIcons.CreditCard, PayslipsRoute, listOf(PayslipsRoute::class));

    companion object {

        /**
         * Tabs of each role. Both bars end with "Más": HR finds its own employee view there, and
         * employees find their profile, benefits, organization chart, notifications and account.
         *
         * @param role role of the signed-in user.
         */
        fun forRole(role: UserRole): List<TopLevelDestination> = when (role) {
            UserRole.HUMAN_RESOURCES -> listOf(PANEL, PERSONAL, REQUESTS, ATTENDANCE, MORE)
            UserRole.EMPLOYEE -> listOf(HOME, REQUESTS, ATTENDANCE, PAYSLIPS, MORE)
        }
    }
}
