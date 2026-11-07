package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

private val passwordBackground = Color(0xFFF8F9FF)
private val passwordText = Color(0xFF202124)
private val passwordSecondary = Color(0xFF4B4F58)

@Composable
fun PasswordChangeScreen(
    mandatory: Boolean,
    onBack: () -> Unit,
    vm: IamViewModel = hiltViewModel()
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()
    val loading by vm.loading.collectAsState()

    val valid = IamViewModel.validPassword(next)
    val matches = next.isNotEmpty() && next == confirm
    val canSubmit = !loading && current.isNotBlank() && valid && matches

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(passwordBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 22.dp, bottom = 112.dp)
        ) {
            if (!mandatory) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = passwordSecondary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Cambiar contraseña", fontSize = 27.sp, color = passwordText)
                }
            } else {
                Text("Crea tu contraseña", fontSize = 27.sp, color = passwordText)
            }

            Spacer(Modifier.height(26.dp))

            if (mandatory) {
                Surface(color = Color(0xFFD7E4FA), shape = RoundedCornerShape(14.dp)) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("Ingresaste con una contraseña temporal. Crea una propia para continuar.")
                    }
                }
                Spacer(Modifier.height(22.dp))
            }

            PasswordField(
                current,
                { current = it; vm.clearFeedback() },
                if (mandatory) "Contraseña temporal" else "Contraseña actual"
            )
            Spacer(Modifier.height(20.dp))
            PasswordField(
                next,
                { next = it; vm.clearFeedback() },
                "Nueva contraseña",
                next.isNotEmpty() && !valid
            )
            Spacer(Modifier.height(20.dp))
            PasswordField(
                confirm,
                { confirm = it; vm.clearFeedback() },
                "Confirmar nueva contraseña",
                confirm.isNotEmpty() && !matches
            )

            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E3EB))
            ) {
                Column(
                    Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Text("Tu contraseña debe tener", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = passwordText)
                    PasswordRequirement("Al menos 8 caracteres", next.length >= 8)
                    PasswordRequirement("Una mayúscula y una minúscula", next.any(Char::isUpperCase) && next.any(Char::isLowerCase))
                    PasswordRequirement("Al menos un número", next.any(Char::isDigit))
                    PasswordRequirement("Al menos un símbolo (!@#$%)", next.any { !it.isLetterOrDigit() })
                }
            }

            if (error != null) {
                Spacer(Modifier.height(16.dp))
                ErrorBanner(error!!)
            }
            if (message != null) {
                Spacer(Modifier.height(16.dp))
                Surface(color = Color(0xFF303236), shape = RoundedCornerShape(6.dp)) {
                    Text(message!!, color = Color.White, modifier = Modifier.fillMaxWidth().padding(16.dp))
                }
            }
            if (message != null && !mandatory) {
                TextButton(onClick = onBack) { Text("Volver a cuenta", color = brand) }
            }
        }

        Button(
            onClick = { vm.updatePassword(current, next, confirm) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .height(54.dp),
            shape = RoundedCornerShape(10.dp),
            enabled = canSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = brand)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else {
                Text(if (mandatory) "Guardar y continuar" else "Guardar contraseña", fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PasswordRequirement(label: String, fulfilled: Boolean) {
    val marker = if (fulfilled) Color(0xFF21803A) else passwordSecondary
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (fulfilled) Icons.Default.Check else Icons.Default.Close,
            contentDescription = if (fulfilled) "Cumple" else "Pendiente",
            tint = marker,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(label, color = if (fulfilled) passwordText else passwordSecondary, fontSize = 15.sp)
    }
}
