package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel
import androidx.compose.runtime.collectAsState


@Composable
fun AccountSecurityScreen(onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    var page by remember { mutableStateOf("account") }
    var logoutPrompt by remember { mutableStateOf(false) }
    val biometrics = vm.biometricEnabled()
    val session by vm.session.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()
    if (page == "password") { PasswordChangeScreen(false, { page = "account" }, vm); return }
    if (page == "privacy") { LegalScreen({ page = "account" }); return }
    if (page == "accounts") { AdminAccountsScreen({ page = "account" }, vm); return }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        TextButton(onClick = onBack) { Text("‹  Cuenta y seguridad") }
        Spacer(Modifier.height(22.dp))
        Text(session?.username.orEmpty(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(if (session?.isHr == true) "Personal de RR.HH." else "Colaborador", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(34.dp))
        Text("Seguridad", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text("Desbloqueo con huella"); Text("Requiere integrar BiometricPrompt para activar", style = MaterialTheme.typography.bodySmall) }
            Switch(checked = biometrics, onCheckedChange = { }, enabled = false)
        }
        Item("Cambiar contraseña") { page = "password" }
        if (session?.isHr == true) Item("Cuentas y roles de colaboradores") { page = "accounts" }
        HorizontalDivider()
        Text("Preferencias", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 18.dp))
        Item("Idioma · Español") { page = "language" }
        if (page == "language") { Text("Idioma del dispositivo: Español. Traducciones completas pendientes de integración."); TextButton(onClick = { page = "account" }) { Text("Cerrar") } }
        Item("Notificaciones · Configuración del dispositivo") { page = "notifications" }
        if (page == "notifications") { Text("Las notificaciones se administran en los ajustes de Android."); TextButton(onClick = { page = "account" }) { Text("Cerrar") } }
        HorizontalDivider()
        Item("Términos del servicio y privacidad") { page = "privacy" }
        Spacer(Modifier.height(80.dp))
        OutlinedButton(onClick = { logoutPrompt = true }, modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión", color = androidx.compose.ui.graphics.Color.Red) }
        if (error != null) ErrorBanner(error!!)
        if (message != null) Text(message!!)
    }
    if (logoutPrompt) AlertDialog(onDismissRequest = { logoutPrompt = false }, title = { Text("¿Cerrar sesión?") }, text = { Text("Tu sesión terminará en este dispositivo y se borrarán los datos de acceso guardados.") }, confirmButton = { TextButton(onClick = { logoutPrompt = false; vm.logout() }) { Text("Cerrar sesión") } }, dismissButton = { TextButton(onClick = { logoutPrompt = false }) { Text("Cancelar") } })
}

@Composable private fun Item(label: String, onClick: () -> Unit) { TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp)) { Text(label, modifier = Modifier.weight(1f), textAlign = TextAlign.Start, color = MaterialTheme.colorScheme.onSurface); Text("›", color = brand) } }

