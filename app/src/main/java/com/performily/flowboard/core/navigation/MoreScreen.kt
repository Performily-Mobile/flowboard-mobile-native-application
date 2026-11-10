package com.performily.flowboard.core.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.component.FlowboardListDivider
import com.performily.flowboard.core.designsystem.component.FlowboardListItem
import com.performily.flowboard.core.designsystem.component.FlowboardListSection
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.session.UserRole

/**
 * "Más" screen of both roles (MA-19), with the options that are not in the bottom bar.
 *
 * HR sees its management tools, its own employee view and the general options. Employees, whose
 * bar already has Inicio, Solicitudes, Asistencia and Boletas, see their profile and record, their
 * benefits and vacation balance, the read-only organization chart, the notifications and the
 * account options.
 *
 * @param role role of the signed-in user.
 * @param onOrganizationClick opens areas, positions and organization chart management (HR).
 * @param onRequestTypesClick opens the request types (HR).
 * @param onBenefitsClick opens the benefits administration (HR).
 * @param onPayslipsClick opens payslips and payments (HR).
 * @param onWellbeingClick opens wellbeing (HR).
 * @param onMyProfileClick opens the own profile.
 * @param onMyRecordClick opens the own record.
 * @param onMyBenefitsClick opens the own benefits.
 * @param onMyVacationBalanceClick opens the own vacation balance.
 * @param onMyRequestsClick opens the own requests (HR, employees have the tab).
 * @param onMyAttendanceClick opens the own attendance (HR, employees have the tab).
 * @param onOrganizationChartClick opens the read-only organization chart.
 * @param onNotificationsClick opens the notifications.
 * @param onAccountSecurityClick opens account and security, where the user signs out.
 */
@Composable
fun MoreScreen(
    role: UserRole,
    onOrganizationClick: () -> Unit,
    onRequestTypesClick: () -> Unit,
    onBenefitsClick: () -> Unit,
    onPayslipsClick: () -> Unit,
    onWellbeingClick: () -> Unit,
    onMyProfileClick: () -> Unit,
    onMyRecordClick: () -> Unit,
    onMyBenefitsClick: () -> Unit,
    onMyVacationBalanceClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onMyAttendanceClick: () -> Unit,
    onOrganizationChartClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onAccountSecurityClick: () -> Unit
) {
    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = "Más"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xs, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
        ) {
            when (role) {
                UserRole.HUMAN_RESOURCES -> {
                    FlowboardListSection("Gestión") {
                        FlowboardListItem(FlowboardIcons.AccountTree, "Organización", subtitle = "Áreas, posiciones y organigrama", onClick = onOrganizationClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Article, "Tipos de solicitud", subtitle = "Campos, adjuntos y descuento de saldo", onClick = onRequestTypesClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Gift, "Beneficios", subtitle = "Catálogo, asignación y entregas", onClick = onBenefitsClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.CreditCard, "Boletas y pagos", subtitle = "Carga, publicación y estado de depósito", onClick = onPayslipsClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Favorite, "Bienestar", subtitle = "Espacios, dispositivos e indicadores", onClick = onWellbeingClick)
                    }

                    FlowboardListSection("Mi cuenta") {
                        FlowboardListItem(FlowboardIcons.Person, "Mi perfil y autogestión", subtitle = "Ver mi información como colaborador", onClick = onMyProfileClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Gift, "Mis beneficios", subtitle = "Beneficios vigentes y entregados", onClick = onMyBenefitsClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Calendar, "Mi saldo de vacaciones", subtitle = "Días disponibles y movimientos", onClick = onMyVacationBalanceClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Article, "Mis solicitudes", subtitle = "Vacaciones, permisos y licencias que pediste", onClick = onMyRequestsClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Schedule, "Mi asistencia", subtitle = "Mis marcaciones e historial personal", onClick = onMyAttendanceClick)
                    }
                }

                UserRole.EMPLOYEE -> {
                    FlowboardListSection("Mi información") {
                        FlowboardListItem(FlowboardIcons.Person, "Mi perfil", subtitle = "Datos personales, contrato y puesto", onClick = onMyProfileClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Folder, "Mi expediente", subtitle = "Documentos de tu legajo", onClick = onMyRecordClick)
                    }

                    FlowboardListSection("Beneficios") {
                        FlowboardListItem(FlowboardIcons.Gift, "Mis beneficios", subtitle = "Beneficios vigentes y entregados", onClick = onMyBenefitsClick)
                        FlowboardListDivider()
                        FlowboardListItem(FlowboardIcons.Calendar, "Mi saldo de vacaciones", subtitle = "Días disponibles y movimientos", onClick = onMyVacationBalanceClick)
                    }

                    FlowboardListSection("Empresa") {
                        FlowboardListItem(FlowboardIcons.AccountTree, "Organigrama", subtitle = "Quién es quién en cada área", onClick = onOrganizationChartClick)
                    }
                }
            }

            FlowboardListSection("General") {
                FlowboardListItem(FlowboardIcons.Notifications, "Notificaciones", subtitle = "Avisos de tus solicitudes, boletas y beneficios", onClick = onNotificationsClick)
                FlowboardListDivider()
                FlowboardListItem(FlowboardIcons.Lock, "Cuenta y seguridad", subtitle = "Contraseña, privacidad y cierre de sesión", onClick = onAccountSecurityClick)
            }
        }
    }
}
