package com.performily.flowboard.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar

/**
 * More options screen for HR (MA-19).
 *
 * Options are grouped in two tonal blocks, "Gestión" and "Mi cuenta"; sections not integrated yet
 * open the pending screen with their title.
 *
 * @param onOrganizationClick opens the organization.
 * @param onMyProfileClick opens the own profile.
 * @param onPayslipsClick opens payslips and payments.
 * @param onWellbeingClick opens wellbeing.
 * @param onBenefitsClick opens the benefits administration.
 * @param onMyBenefitsClick opens the own benefits.
 * @param onMyVacationBalanceClick opens the own vacation balance.
 * @param onPendingClick opens the pending screen with the given title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onOrganizationClick: () -> Unit,
    onMyProfileClick: () -> Unit,
    onPayslipsClick: () -> Unit,
    onWellbeingClick: () -> Unit,
    onBenefitsClick: () -> Unit,
    onMyBenefitsClick: () -> Unit,
    onMyVacationBalanceClick: () -> Unit,
    onPendingClick: (title: String) -> Unit
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
            MoreSection("Gestión") {
                MoreItem(FlowboardIcons.AccountTree, "Organización", "Áreas, posiciones y organigrama", onOrganizationClick)
                MoreDivider()
                MoreItem(FlowboardIcons.Article, "Tipos de solicitud", "Campos, adjuntos y descuento de saldo") {
                    onPendingClick("Tipos de solicitud")
                }
                MoreDivider()
                MoreItem(FlowboardIcons.Gift, "Beneficios", "Catálogo, asignación y entregas", onBenefitsClick)
                MoreDivider()
                MoreItem(FlowboardIcons.CreditCard, "Boletas y pagos", "Carga, publicación y estado de depósito", onPayslipsClick)
                MoreDivider()
                MoreItem(FlowboardIcons.Favorite, "Bienestar", "Espacios, dispositivos e indicadores", onWellbeingClick)
            }

            MoreSection("Mi cuenta") {
                MoreItem(FlowboardIcons.Person, "Mi perfil y autogestión", "Ver mi información como colaborador", onMyProfileClick)
                MoreDivider()
                MoreItem(FlowboardIcons.Gift, "Mis beneficios", "Beneficios vigentes y entregados", onMyBenefitsClick)
                MoreDivider()
                MoreItem(FlowboardIcons.Schedule, "Mi saldo de vacaciones", "Días disponibles y movimientos", onMyVacationBalanceClick)
                MoreDivider()
                MoreItem(FlowboardIcons.Lock, "Cuenta y seguridad", "Huella, idioma y cierre de sesión") {
                    onPendingClick("Cuenta y seguridad")
                }
            }
        }
    }
}

/**
 * Titled group of options shown inside a tonal card.
 *
 * @param title title of the group.
 * @param content options of the group.
 */
@Composable
private fun MoreSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = Spacing.xs)
        )
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

/** Thin divider between two options, inset so it starts after the icon. */
@Composable
private fun MoreDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp, end = Spacing.l),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * One option: round icon, title, short description and a chevron.
 *
 * @param icon icon of the option.
 * @param title name of the option.
 * @param subtitle short description of the option.
 * @param onClick opens the option.
 */
@Composable
private fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.l, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.l),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = FlowboardIcons.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
