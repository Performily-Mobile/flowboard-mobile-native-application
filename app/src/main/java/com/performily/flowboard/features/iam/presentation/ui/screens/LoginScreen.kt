package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardLogo
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.iam.presentation.ui.components.canUseStrongBiometric
import com.performily.flowboard.features.iam.presentation.ui.components.findFragmentActivity
import com.performily.flowboard.features.iam.presentation.ui.components.showBiometricPrompt
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

@Composable
fun LoginScreen(vm: IamViewModel = hiltViewModel()) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var recovery by remember { mutableStateOf(false) }
    var showLegal by remember { mutableStateOf(false) }
    val error by vm.error.collectAsState()
    val loading by vm.loading.collectAsState()
    val context = LocalContext.current
    val activity = remember(context) { context.findFragmentActivity() }
    val biometricReady = remember(context) { context.canUseStrongBiometric() }
    var hasBiometricLogin by remember { mutableStateOf(vm.hasBiometricLogin()) }
    var enableBiometric by remember { mutableStateOf(false) }

    fun submitPassword() {
        val cipher = if (enableBiometric) vm.biometricEnrollCipher() else null
        if (enableBiometric && cipher != null && activity != null) {
            activity.showBiometricPrompt(
                title = "Activar ingreso con huella",
                subtitle = "Confirma tu huella para usarla en tus próximos ingresos",
                cipher = cipher,
                onSuccess = { authorized -> vm.loginAndEnableBiometric(username, password, authorized) },
                onError = vm::showError
            )
        } else {
            vm.login(username, password)
        }
    }

    fun submitBiometric() {
        val cipher = vm.biometricLoginCipher()
        if (cipher == null || activity == null) {
            hasBiometricLogin = vm.hasBiometricLogin()
            return
        }
        activity.showBiometricPrompt(
            title = "Ingresar con huella",
            subtitle = "Usa tu huella para entrar a Flowboard",
            cipher = cipher,
            onSuccess = vm::loginWithBiometric,
            onError = vm::showError
        )
    }

    LaunchedEffect(error) {
        hasBiometricLogin = vm.hasBiometricLogin()
    }

    if (showLegal) {
        LegalScreen(onBack = { showLegal = false })
        return
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
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
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) { Text("Volver a iniciar sesión") }
            } else {
                Spacer(Modifier.height(45.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.s)
                ) {
                    FlowboardLogo()
                    Text(
                        "Flowboard",
                        color = brand,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
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
                if (biometricReady && !hasBiometricLogin) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { enableBiometric = !enableBiometric }
                            .padding(vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = brand)
                        Text(
                            text = "Activar ingreso con huella",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = Spacing.m)
                        )
                        Switch(checked = enableBiometric, onCheckedChange = { enableBiometric = it })
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { submitPassword() },
                    enabled = username.isNotBlank() && password.isNotBlank() && !loading,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(if (loading) "Ingresando…" else "Ingresar")
                }
                if (biometricReady && hasBiometricLogin) {
                    Spacer(Modifier.height(22.dp))
                    LoginDivider()
                    Spacer(Modifier.height(16.dp))
                    BiometricLoginButton(enabled = !loading, onClick = { submitBiometric() })
                }

            }
        }

        if (!recovery) {

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
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


/**
 * Button that signs in with the fingerprint. It only appears when the device has a fingerprint
 * enrolled and the user activated the fingerprint sign-in, so it is never shown disabled.
 *
 * @param enabled false while a sign-in is in progress.
 * @param onClick called when the button is tapped.
 */
@Composable
private fun BiometricLoginButton(enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
