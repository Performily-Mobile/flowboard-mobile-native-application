package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

private val accountBackground = Color(0xFFF8F9FF)
private val accountText = Color(0xFF202124)
private val accountSecondary = Color(0xFF4B4F58)
private val accountIconBackground = Color(0xFFD7E4FA)
private val accountDivider = Color(0xFFE0E3EB)
private val accountDanger = Color(0xFFC51C1C)

/**
 * Account settings belong to IAM. The session provides email and role, but not a
 * verified full name; the avatar label is only a display name derived from the email.
 */
@Composable
fun AccountSecurityScreen(
    onBack: () -> Unit,
    vm: IamViewModel = hiltViewModel()
) {
    var page by remember { mutableStateOf("account") }
    var logoutPrompt by remember { mutableStateOf(false) }
    var languageDialog by remember { mutableStateOf(false) }
    var notificationsDialog by remember { mutableStateOf(false) }

    val session by vm.session.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()

    if (page == "password") {
        PasswordChangeScreen(false, { page = "account" }, vm)
        return
    }
    if (page == "privacy") {
        LegalScreen(onBack = { page = "account" })
        return
    }
    if (page == "accounts") {
        AdminAccountsScreen(onBack = { page = "account" }, vm = vm)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(accountBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 112.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = accountSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Text("Cuenta y seguridad", color = accountText, fontSize = 27.sp)
            }

            Spacer(Modifier.height(34.dp))
            val email = session?.username.orEmpty()
            val displayName = accountDisplayName(email)
            val initials = accountInitials(displayName)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(accountIconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(initials, color = Color(0xFF365A82), fontSize = 23.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        displayName,
                        color = accountText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        email.ifBlank { "Sin correo registrado" },
                        color = accountSecondary,
                        fontSize = 14.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        if (session?.isHr == true) "Personal de RR.HH." else "Colaborador",
                        color = accountSecondary,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
            SettingsSectionTitle("Seguridad")
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(30.dp), tint = accountSecondary)
                Spacer(Modifier.width(18.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Desbloqueo con huella", color = accountText, fontSize = 18.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Disponible cuando se integre la autenticación biométrica",
                        color = accountSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = false,
                    onCheckedChange = null,
                    enabled = false
                )
            }

            SettingsRow(
                icon = Icons.Default.Lock,
                title = "Cambiar contraseña",
                onClick = { vm.clearFeedback(); page = "password" }
            )

            // HR-only capability, kept even though the employee mockup does not show it.
            if (session?.isHr == true) {
                SettingsRow(
                    icon = Icons.Default.Groups,
                    title = "Cuentas y roles de colaboradores",
                    onClick = { vm.clearFeedback(); page = "accounts" }
                )
            }

            HorizontalDivider(color = accountDivider)
            Spacer(Modifier.height(12.dp))
            SettingsSectionTitle("Preferencias")

            SettingsRow(
                icon = Icons.Default.Language,
                title = "Idioma",
                subtitle = "Español (es_419)",
                onClick = { languageDialog = true }
            )
            HorizontalDivider(color = accountDivider)

            SettingsRow(
                icon = Icons.Default.NotificationsNone,
                title = "Notificaciones",
                subtitle = "Configuración del dispositivo",
                onClick = { notificationsDialog = true }
            )
            HorizontalDivider(color = accountDivider)

            Spacer(Modifier.height(12.dp))
            SettingsSectionTitle("Acerca de")
            SettingsRow(
                icon = Icons.Default.Description,
                title = "Términos del servicio y privacidad",
                subtitle = "Ley N.° 29733 de Protección de Datos Personales",
                onClick = { page = "privacy" }
            )
            HorizontalDivider(color = accountDivider)

            if (error != null) {
                Spacer(Modifier.height(12.dp))
                ErrorBanner(error!!)
            }
            if (message != null) {
                Spacer(Modifier.height(12.dp))
                Text(message!!, color = brand)
            }
        }

        OutlinedButton(
            onClick = { logoutPrompt = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, accountDanger),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accountDanger)
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Cerrar sesión", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }
    }

    if (logoutPrompt) {
        AlertDialog(
            onDismissRequest = { logoutPrompt = false },
            title = { Text("¿Cerrar sesión?") },
            text = {
                Text("Tu sesión terminará en este dispositivo y se borrarán los datos de acceso guardados. Necesitarás tu contraseña para volver a ingresar.")
            },
            confirmButton = {
                TextButton(onClick = { logoutPrompt = false; vm.logout() }) {
                    Text("Cerrar sesión", color = accountDanger)
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
            title = { Text("Notificaciones") },
            text = { Text("Por ahora, las notificaciones se administran desde los ajustes de Android.") },
            confirmButton = { TextButton(onClick = { notificationsDialog = false }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        color = accountText,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(accountIconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = Color(0xFF182C42), modifier = Modifier.size(23.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = accountText, fontSize = 17.sp, lineHeight = 22.sp)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(subtitle, color = accountSecondary, fontSize = 14.sp, lineHeight = 19.sp)
            }
        }
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = accountSecondary)
    }
}

private fun accountDisplayName(email: String): String {
    val localPart = email.substringBefore('@')
    val parts = localPart.split('.', '_', '-').filter { it.isNotBlank() }
    if (parts.isEmpty()) return "Mi cuenta"
    return parts.joinToString(" ") { part ->
        part.replaceFirstChar { it.uppercase() }
    }
}

private fun accountInitials(name: String): String =
    name.split(' ').filter { it.isNotBlank() }.take(2).joinToString("") { it.take(1).uppercase() }
