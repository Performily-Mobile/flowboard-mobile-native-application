package com.performily.flowboard.core.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.session.UserRole
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
import kotlin.reflect.KClass

/**
 * Pestañas de la barra inferior.
 * route: a dónde lleva la pestaña.
 * matches: pantallas en las que la pestaña se ve seleccionada.
 */
enum class TopLevelDestination(
    val label: String,
    val icon: ImageVector,
    val route: Any,
    val matches: List<KClass<*>>
) {
    // ---------- RR.HH. (MA-18 a MA-20) ----------
    PANEL("Panel", FlowboardIcons.GridView, PanelRoute, listOf(PanelRoute::class)),
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
    REQUESTS("Solicitudes", FlowboardIcons.Article, RequestsRoute, listOf(RequestsRoute::class)),
    ATTENDANCE("Asistencia", FlowboardIcons.Schedule, AttendanceRoute, listOf(AttendanceRoute::class)),
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
            ReadingHistoryRoute::class
        )
    ),

    // ---------- Colaborador (MA-14 a MA-17) ----------
    HOME("Inicio", FlowboardIcons.Home, HomeRoute, listOf(HomeRoute::class)),
    PAYSLIPS("Boletas", FlowboardIcons.CreditCard, PayslipsRoute, listOf(PayslipsRoute::class)),
    PROFILE(
        "Perfil",
        FlowboardIcons.Person,
        MyProfileRoute,
        listOf(MyProfileRoute::class, MyRecordRoute::class, OrganizationChartRoute::class)
    );

    companion object {

        /**
         * RR.HH. tiene su barra de gestión; su vista de colaborador
         * (Mi perfil, Mi expediente) está en "Más" › "Mi perfil y autogestión".
         */
        fun forRole(role: UserRole): List<TopLevelDestination> = when (role) {
            UserRole.HUMAN_RESOURCES -> listOf(PANEL, PERSONAL, REQUESTS, ATTENDANCE, MORE)
            UserRole.EMPLOYEE -> listOf(HOME, REQUESTS, ATTENDANCE, PAYSLIPS, PROFILE)
        }
    }
}
