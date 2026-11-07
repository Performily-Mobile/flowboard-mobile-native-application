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

