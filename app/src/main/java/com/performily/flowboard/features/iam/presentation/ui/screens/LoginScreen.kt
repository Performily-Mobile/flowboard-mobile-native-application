package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

@Composable
fun LoginScreen(vm: IamViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()

    if (showLegal) {
        LegalScreen(onBack = { showLegal = false })
        return
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FF))
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 24.dp)
                .padding(bottom = 72.dp)
        ) {

            if (recovery) {
                TextButton(onClick = { recovery = false }) {
                    Text(
                        "‹  Recuperar acceso",
                        color = brand
                    )
                }
                Spacer(Modifier.height(55.dp))
                Icon(
                    Icons.Default.Lock,
                    null,
                    tint = brand,
                    modifier = Modifier.align(Alignment.CenterHorizontally).size(70.dp)
                )
                Spacer(Modifier.height(28.dp))
                Text(
                    "Recursos Humanos restablece tu contraseña",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    "Por seguridad, el personal de RR.HH. genera una contraseña temporal que llega a tu correo corporativo. Al ingresar con ella se te pedirá crear una nueva.",
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                OutlinedCard {
                    Column(
                        Modifier.fillMaxWidth().padding(18.dp)
                    ) {
                        Text(
                            "Contacto de Recursos Humanos",
                            fontWeight = FontWeight.Bold
                        ); Text("Solicita el contacto oficial a tu organización.")
                    }
                }
                Spacer(Modifier.height(48.dp))
                Button(
                    onClick = { recovery = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = brand)
                ) { Text("Volver a iniciar sesión") }
            } else {
                Spacer(Modifier.height(45.dp))
                Text(
                    "▰  Flowboard",
                    color = brand,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                Text("Iniciar sesión", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Ingresa con las credenciales que Recursos Humanos te envió por correo.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(22.dp))

                if (error != null) {
                    ErrorBanner(error!!)
                    Spacer(Modifier.height(18.dp))
                }

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; vm.clearFeedback() },
                    label = { Text("Correo corporativo") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = error != null
                )
                Spacer(Modifier.height(16.dp))
                PasswordField(
                    value = password,
                    onChange = { password = it; vm.clearFeedback() },
                    label = "Contraseña",
                    error = error != null,
                    supportingError = if (error != null) "Usuario o contraseña incorrectos" else null
                )
                TextButton(
                    onClick = { recovery = true; vm.clearFeedback() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Olvidé mi contraseña", color = brand)
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { vm.login(username, password) },
                    enabled = username.isNotBlank() && password.isNotBlank() && !loading,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = brand)
                ) {
                    Text(if (loading) "Ingresando…" else "Ingresar")
                }
                Spacer(Modifier.height(22.dp))

                LoginDivider()

                Spacer(Modifier.height(16.dp))
                BiometricPlaceholderButton()

            }
        }

        if (!recovery) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        horizontal = 28.dp,
                        vertical = 16.dp
                    )
            ) {
                LoginFooter(
                    onPrivacyClick = { showLegal = true }
                )
            }
        }
    }
}


@Composable
private fun LoginDivider() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "o",
            modifier = Modifier.padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
    }
}


@Composable
private fun BiometricPlaceholderButton() {
    OutlinedButton(
        onClick = {},
        enabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            Color(0xFFB0B0B0)
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            disabledContainerColor = Color(0xFFF2F2F2),
            disabledContentColor = Color(0xFF9E9E9E)
        )
    ) {
        Icon(
            Icons.Default.Fingerprint,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text("Ingresar con huella digital")
    }
}


@Composable
private fun LoginFooter(onPrivacyClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Language, contentDescription = null, tint = brand)
            Spacer(Modifier.width(8.dp))
            Text("ES / EN", color = brand)
        }

        Text(
            text = "Términos y privacidad",
            color = brand,
            modifier = Modifier.clickable(onClick = onPrivacyClick)
        )
    }
}
