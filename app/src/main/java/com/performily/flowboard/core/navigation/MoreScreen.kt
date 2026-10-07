package com.performily.flowboard.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider

/** MA-19 · Más opciones (RR.HH.). */
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
            TopAppBar(
                title = { Text("Más") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            MoreSectionTitle("Gestión")
            MoreItem(FlowboardIcons.AccountTree, "Organización", "Áreas, posiciones y organigrama", onOrganizationClick)
            MoreItem(FlowboardIcons.Article, "Tipos de solicitud", "Campos, adjuntos y descuento de saldo") {
                onPendingClick("Tipos de solicitud")
            }
            MoreItem(FlowboardIcons.Gift, "Beneficios", "Catálogo, asignación y entregas", onBenefitsClick)
            MoreItem(FlowboardIcons.CreditCard, "Boletas y pagos", "Carga, publicación y estado de depósito", onPayslipsClick)
            MoreItem(FlowboardIcons.Favorite, "Bienestar", "Espacios, dispositivos e indicadores", onWellbeingClick)

            MoreSectionTitle("Mi cuenta")
            MoreItem(FlowboardIcons.Person, "Mi perfil y autogestión", "Ver mi información como colaborador", onMyProfileClick)
            MoreItem(FlowboardIcons.Gift, "Mis beneficios", "Beneficios vigentes y entregados", onMyBenefitsClick)
            MoreItem(FlowboardIcons.Schedule, "Mi saldo de vacaciones", "Días disponibles y movimientos", onMyVacationBalanceClick)
            MoreItem(FlowboardIcons.Lock, "Cuenta y seguridad", "Huella, idioma y cierre de sesión") {
                onPendingClick("Cuenta y seguridad")
            }
        }
    }
}

@Composable
private fun MoreSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun MoreItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = FlowboardIcons.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider(color = Divider)
    }
}
