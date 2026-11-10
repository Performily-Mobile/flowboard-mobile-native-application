package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.component.FlowboardListDivider
import com.performily.flowboard.core.designsystem.component.FlowboardListItem
import com.performily.flowboard.core.designsystem.component.FlowboardListSection
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

/**
 * Account and security settings of the signed-in user: password, language, legal terms and sign
 * out. HR staff also manage the accounts and roles of the employees from here.
 *
 * The session provides the e-mail and the role but not a verified full name, so the name shown is
 * derived from the e-mail.
 *
 * @param onBack called when the back button is tapped.
 * @param vm IAM view model.
 */
@Composable
fun AccountSecurityScreen(
    onBack: () -> Unit,
    vm: IamViewModel = hiltViewModel()
) {
    var page by remember { mutableStateOf(AccountPage.ACCOUNT) }
    var logoutPrompt by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    var notificationsDialog by remember { mutableStateOf(false) }

    val session by vm.session.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()

    when (page) {
        AccountPage.PASSWORD -> {
            PasswordChangeScreen(mandatory = false, onBack = { page = AccountPage.ACCOUNT }, vm = vm)
            return
        }
        AccountPage.PRIVACY -> {
            LegalScreen(onBack = { page = AccountPage.ACCOUNT })
            return
        }
        AccountPage.ACCOUNTS -> {
            AdminAccountsScreen(onBack = { page = AccountPage.ACCOUNT }, vm = vm)
            return
        }
        AccountPage.ACCOUNT -> Unit
    }

    val email = session?.username.orEmpty()
    val displayName = accountDisplayName(email)

    Scaffold(
        topBar = { FlowboardTopBar(title = "Cuenta y seguridad", onBack = onBack) },
        bottomBar = {
            FlowboardBottomActionBar {
                OutlinedButton(
                    onClick = { logoutPrompt = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(Spacing.s))
                    Text("Cerrar sesión")
                }
            }
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
            ProfileCard(
                name = displayName,
                email = email.ifBlank { "Sin correo registrado" },
                role = if (session?.isHr == true) "Personal de RR. HH." else "Colaborador"
            )

            FlowboardListSection("Seguridad") {
                FlowboardListItem(
                    icon = Icons.Default.Fingerprint,
                    title = "Desbloqueo con huella",
                    subtitle = "Disponible cuando se integre la autenticación biométrica",
                    trailing = { Switch(checked = false, onCheckedChange = null, enabled = false) }
                )
                FlowboardListDivider()
                FlowboardListItem(
                    icon = Icons.Default.Lock,
                    title = "Cambiar contraseña",
                    onClick = {
                        vm.clearFeedback()
                        page = AccountPage.PASSWORD
                    }
                )
                if (session?.isHr == true) {
                    FlowboardListDivider()
                    FlowboardListItem(
                        icon = Icons.Default.Groups,
                        title = "Cuentas y roles de colaboradores",
                        onClick = {
                            vm.clearFeedback()
                            page = AccountPage.ACCOUNTS
                        }
                    )
                }
            }

            FlowboardListSection("Preferencias") {
                FlowboardListItem(
                    icon = Icons.Default.Language,
                    title = "Idioma",
                    subtitle = "Español (es_419)",
                    onClick = { languageDialog = true }
                )
                FlowboardListDivider()
                FlowboardListItem(
                    icon = Icons.Default.NotificationsNone,
                    title = "Notificaciones del dispositivo",
                    subtitle = "Sonido y avisos de Android",
                    onClick = { notificationsDialog = true }
                )
            }

            FlowboardListSection("Acerca de") {
                FlowboardListItem(
                    icon = Icons.Default.Description,
                    title = "Términos del servicio y privacidad",
                    subtitle = "Ley N.° 29733 de Protección de Datos Personales",
                    onClick = { page = AccountPage.PRIVACY }
                )
            }

            error?.let { ErrorBanner(it) }
            message?.let { SuccessBanner(it) }
        }
    }

    if (logoutPrompt) {
        AlertDialog(
            onDismissRequest = { logoutPrompt = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text(
                    "Tu sesión terminará en este dispositivo y se borrarán los datos de acceso guardados. " +
                        "Necesitarás tu contraseña para volver a ingresar."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        logoutPrompt = false
                        vm.logout()
                    }
                ) {
                    Text("Cerrar sesión", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logoutPrompt = false }) { Text("Cancelar") }
            }
        )
    }

    if (languageDialog) {
        AlertDialog(
            onDismissRequest = { languageDialog = false },
            title = { Text("Idioma de la aplicación") },
            text = { Text("La traducción completa al inglés todavía no está disponible. La aplicación permanece en español.") },
            confirmButton = { TextButton(onClick = { languageDialog = false }) { Text("Entendido") } }
        )
    }

    if (notificationsDialog) {
        AlertDialog(
            onDismissRequest = { notificationsDialog = false },
            title = { Text("Notificaciones del dispositivo") },
            text = { Text("El sonido y los avisos se administran desde los ajustes de Android.") },
            confirmButton = { TextButton(onClick = { notificationsDialog = false }) { Text("Entendido") } }
        )
    }
}

/** Sub-pages shown inside the account screen. */
private enum class AccountPage { ACCOUNT, PASSWORD, PRIVACY, ACCOUNTS }

/**
 * Card with the initials, name, e-mail and role of the signed-in user.
 *
 * @param name name derived from the e-mail.
 * @param email e-mail of the account.
 * @param role readable role of the account.
 */
@Composable
private fun ProfileCard(name: String, email: String, role: String) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(Spacing.l),
            horizontalArrangement = Arrangement.spacedBy(Spacing.l),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = accountInitials(name),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = role,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Builds a readable name from the local part of an e-mail, for example "Luis Gutierrez".
 *
 * @param email e-mail of the account.
 */
private fun accountDisplayName(email: String): String {
    val parts = email.substringBefore('@').split('.', '_', '-').filter { it.isNotBlank() }
    if (parts.isEmpty()) return "Mi cuenta"
    return parts.joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }
}

/**
 * Takes the first letter of up to two words of a name, for example "LG".
 *
 * @param name name to abbreviate.
 */
private fun accountInitials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
