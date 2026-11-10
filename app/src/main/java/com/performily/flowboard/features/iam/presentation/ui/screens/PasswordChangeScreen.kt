package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardBottomActionBar
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

/**
 * Password change. When [mandatory] is true the user signed in with a temporary password and must
 * create their own before entering the app, so there is no back button.
 *
 * @param mandatory true for the first sign-in with a temporary password.
 * @param onBack called when the back button or "Volver a cuenta" is tapped.
 * @param vm IAM view model.
 */
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

    Scaffold(
        topBar = {
            FlowboardTopBar(
                title = if (mandatory) "Crea tu contraseña" else "Cambiar contraseña",
                onBack = if (mandatory) null else onBack
            )
        },
        bottomBar = {
            FlowboardBottomActionBar {
                Button(
                    onClick = { vm.updatePassword(current, next, confirm) },
                    enabled = canSubmit,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (mandatory) "Guardar y continuar" else "Guardar contraseña")
                    }
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
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            if (mandatory) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.l, vertical = Spacing.m),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(FlowboardIcons.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        Text(
                            text = "Ingresaste con una contraseña temporal. Crea una propia para continuar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            PasswordField(
                value = current,
                onChange = {
                    current = it
                    vm.clearFeedback()
                },
                label = if (mandatory) "Contraseña temporal" else "Contraseña actual"
            )
            PasswordField(
                value = next,
                onChange = {
                    next = it
                    vm.clearFeedback()
                },
                label = "Nueva contraseña",
                error = next.isNotEmpty() && !valid
            )
            PasswordField(
                value = confirm,
                onChange = {
                    confirm = it
                    vm.clearFeedback()
                },
                label = "Confirmar nueva contraseña",
                error = confirm.isNotEmpty() && !matches,
                supportingError = "Las contraseñas no coinciden."
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(Spacing.m)
                ) {
                    Text("Tu contraseña debe tener", style = MaterialTheme.typography.titleSmall)
                    PasswordRequirement("Al menos 8 caracteres", next.length >= 8)
                    PasswordRequirement(
                        "Una mayúscula y una minúscula",
                        next.any(Char::isUpperCase) && next.any(Char::isLowerCase)
                    )
                    PasswordRequirement("Al menos un número", next.any(Char::isDigit))
                    PasswordRequirement("Al menos un símbolo (!@#$%)", next.any { !it.isLetterOrDigit() })
                }
            }

            error?.let { ErrorBanner(it) }
            message?.let { SuccessBanner(it) }
            if (message != null && !mandatory) {
                TextButton(onClick = onBack) { Text("Volver a cuenta") }
            }
        }
    }
}

/**
 * One rule of the new password, with a check when it is fulfilled.
 *
 * @param label rule to show.
 * @param fulfilled true when the new password meets the rule.
 */
@Composable
private fun PasswordRequirement(label: String, fulfilled: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = if (fulfilled) ActiveContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = if (fulfilled) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (fulfilled) "Cumple" else "Pendiente",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(4.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (fulfilled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
