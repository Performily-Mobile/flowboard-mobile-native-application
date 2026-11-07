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
fun LoginScreen(vm: IamViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color(0xFFF8F9FF)).verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 28.dp, vertical = 24.dp)) {
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
            Spacer(Modifier.height(48.dp))
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
            Spacer(Modifier.height(48.dp))
            Text("Flowboard · Acceso seguro", color = brand, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

