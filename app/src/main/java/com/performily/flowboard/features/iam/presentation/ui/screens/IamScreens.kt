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

private val brand = androidx.compose.ui.graphics.Color(0xFF3D6292)

@Composable
fun LoginScreen(vm: IamViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xFFF8F9FF)).padding(horizontal = 28.dp, vertical = 24.dp)) {
        if (recovery) {
            TextButton(onClick = { recovery = false }) { Text("‹  Recuperar acceso", color = brand) }
            Spacer(Modifier.height(55.dp))
            Icon(Icons.Default.Lock, null, tint = brand, modifier = Modifier.align(Alignment.CenterHorizontally).size(70.dp))
            Spacer(Modifier.height(28.dp))
            Text("Recursos Humanos restablece tu contraseña", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Text("Por seguridad, el personal de RR.HH. genera una contraseña temporal que llega a tu correo corporativo. Al ingresar con ella se te pedirá crear una nueva.", textAlign = TextAlign.Center)
            Spacer(Modifier.height(28.dp))
            OutlinedCard { Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("Contacto de Recursos Humanos", fontWeight = FontWeight.Bold); Text("Solicita el contacto oficial a tu organización.") } }
            Spacer(Modifier.weight(1f))
            Button(onClick = { recovery = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = brand)) { Text("Volver a iniciar sesión") }
        } else {
            Spacer(Modifier.height(45.dp))
            Text("▰  Flowboard", color = brand, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(12.dp))
            Text("Ingresa con las credenciales que Recursos Humanos te envió por correo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(22.dp))
            if (error != null) { ErrorBanner(error!!); Spacer(Modifier.height(18.dp)) }
            OutlinedTextField(username, { username = it; vm.clearFeedback() }, label = { Text("Correo corporativo") }, leadingIcon = { Icon(Icons.Default.Person, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), isError = error != null)
            Spacer(Modifier.height(16.dp))
            PasswordField(password, { password = it; vm.clearFeedback() }, "Contraseña", error != null)
            TextButton(onClick = { recovery = true; vm.clearFeedback() }, modifier = Modifier.align(Alignment.End)) { Text("Olvidé mi contraseña", color = brand) }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { vm.login(username, password) }, enabled = username.isNotBlank() && password.isNotBlank() && !loading, modifier = Modifier.fillMaxWidth().height(52.dp), colors = ButtonDefaults.buttonColors(containerColor = brand)) { Text(if (loading) "Ingresando…" else "Ingresar") }
            Spacer(Modifier.height(22.dp))
            Text("o", modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { Icon(Icons.Default.Fingerprint, null); Spacer(Modifier.width(8.dp)); Text("Ingresar con huella digital") }
            Text("La huella estará disponible después del primer inicio de sesión.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.weight(1f))
            Text("ES / EN                     Términos y privacidad", color = brand, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun PasswordField(value: String, onChange: (String) -> Unit, label: String, error: Boolean = false) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true, modifier = Modifier.fillMaxWidth(), isError = error,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility, "Mostrar contraseña") } })
}

@Composable
fun ErrorBanner(message: String) { Surface(color = androidx.compose.ui.graphics.Color(0xFFFFD9D4), shape = RoundedCornerShape(14.dp)) { Text("⚠  $message", color = androidx.compose.ui.graphics.Color(0xFF7F1616), modifier = Modifier.fillMaxWidth().padding(16.dp)) } }

@Composable
fun PasswordChangeScreen(mandatory: Boolean, onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        if (!mandatory) TextButton(onClick = onBack) { Text("‹  Volver") }
        Text(if (mandatory) "Crea tu contraseña" else "Cambiar contraseña", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(24.dp))
        if (mandatory) { Surface(color = androidx.compose.ui.graphics.Color(0xFFD7E4FA), shape = RoundedCornerShape(12.dp)) { Text("Ingresaste con una contraseña temporal. Crea una propia para continuar.", Modifier.padding(16.dp)) }; Spacer(Modifier.height(16.dp)) }
        PasswordField(current, { current = it; vm.clearFeedback() }, if (mandatory) "Contraseña temporal" else "Contraseña actual")
        Spacer(Modifier.height(16.dp))
        PasswordField(next, { next = it; vm.clearFeedback() }, "Nueva contraseña", next.isNotEmpty() && !IamViewModel.validPassword(next))
        Spacer(Modifier.height(16.dp))
        PasswordField(confirm, { confirm = it; vm.clearFeedback() }, "Confirmar nueva contraseña", confirm.isNotEmpty() && confirm != next)
        Spacer(Modifier.height(24.dp))
        OutlinedCard { Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Tu contraseña debe tener", fontWeight = FontWeight.Bold)
            Requirement("Al menos 8 caracteres", next.length >= 8)
            Requirement("Una mayúscula y una minúscula", next.any(Char::isUpperCase) && next.any(Char::isLowerCase))
            Requirement("Al menos un número", next.any(Char::isDigit))
            Requirement("Al menos un símbolo (!@#$%)", next.any { !it.isLetterOrDigit() })
        } }
        if (error != null) { Spacer(Modifier.height(12.dp)); ErrorBanner(error!!) }
        if (message != null) { Spacer(Modifier.height(12.dp)); Text(message!!, color = brand) }
        Spacer(Modifier.height(28.dp))
        Button(onClick = { vm.updatePassword(current, next, confirm) }, modifier = Modifier.fillMaxWidth().height(52.dp), enabled = !loading && current.isNotEmpty() && next.isNotEmpty() && confirm.isNotEmpty(), colors = ButtonDefaults.buttonColors(containerColor = brand)) { Text(if (mandatory) "Guardar y continuar" else "Guardar contraseña") }
        if (message != null && !mandatory) TextButton(onClick = onBack) { Text("Volver a cuenta") }
    }
}
@Composable private fun Requirement(label: String, good: Boolean) { Text("${if (good) "✓" else "○"}  $label", color = if (good) androidx.compose.ui.graphics.Color(0xFF24803F) else MaterialTheme.colorScheme.onSurface) }

@Composable
fun AccountSecurityScreen(onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    var page by remember { mutableStateOf("account") }
    var logoutPrompt by remember { mutableStateOf(false) }
    var biometrics by remember { mutableStateOf(false) }
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

@Composable
fun LegalScreen(onBack: () -> Unit) {
    var privacy by remember { mutableStateOf(true) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        TextButton(onClick = onBack) { Text("‹  Términos y privacidad") }
        Row { TextButton(onClick = { privacy = false }) { Text("Términos del servicio") }; TextButton(onClick = { privacy = true }) { Text("Privacidad") } }
        Text(if (privacy) "Política de privacidad" else "Términos del servicio", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text("Referencia de diseño: 01/09/2026", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        if (privacy) {
            LegalParagraph("Datos que tratamos", "Tu ficha laboral, asistencia, solicitudes, beneficios y boletas, registrados por tu organización.")
            LegalParagraph("Quién puede verlos", "Solo tú y el personal de Recursos Humanos autorizado, de acuerdo con tus permisos.")
            LegalParagraph("Marco legal", "Ley N.° 29733, Ley de Protección de Datos Personales, y su reglamento.")
            LegalParagraph("En tu dispositivo", "El token de sesión se guarda cifrado usando Android Keystore.")
        } else LegalParagraph("Uso del servicio", "Flowboard facilita la consulta de información laboral, sujeto a los permisos asignados por tu organización. La versión legal definitiva debe ser aprobada por la organización.")
    }
}
@Composable private fun LegalParagraph(title: String, detail: String) { Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 14.dp)); Text(detail, modifier = Modifier.padding(top = 8.dp)) }

@Composable
fun AdminAccountsScreen(onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    val accounts by vm.accounts.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()
    var selected by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(Unit) { vm.loadAccounts() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(22.dp)) {
        TextButton(onClick = onBack) { Text("‹  Cuentas de colaboradores") }
        if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (error != null) ErrorBanner(error!!)
        if (message != null) Text(message!!, color = brand)
        accounts.forEach { a ->
            OutlinedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text(a.username, fontWeight = FontWeight.SemiBold)
                    Text("${a.role} · ${a.status}")
                    if (a.status == "ACTIVE") {
                        TextButton(onClick = { selected = a.id }) { Text("Cambiar rol") }
                        TextButton(onClick = { vm.resetPassword(a.id) }) { Text("Restablecer contraseña") }
                    }
                }
            }
        }
    }
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text("Asignar rol") }, text = { Text("El backend validará que exista al menos una cuenta activa de RR.HH.") }, confirmButton = { TextButton(onClick = { vm.changeRole(selected!!, "HR_STAFF"); selected = null }) { Text("RR.HH.") } }, dismissButton = { TextButton(onClick = { vm.changeRole(selected!!, "EMPLOYEE"); selected = null }) { Text("Colaborador") } })
}

@Composable
fun RestrictedAccessScreen(onMyProfile: () -> Unit, onHome: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Lock, null, tint = brand, modifier = Modifier.size(75.dp))
        Spacer(Modifier.height(24.dp))
        Text("No tienes acceso a esta información", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Solo puedes consultar tu propia información laboral. El acceso a otros colaboradores está restringido por rol.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onMyProfile, colors = ButtonDefaults.buttonColors(containerColor = brand)) { Text("Ir a mi perfil") }
        TextButton(onClick = onHome) { Text("Volver al inicio") }
    }
}
