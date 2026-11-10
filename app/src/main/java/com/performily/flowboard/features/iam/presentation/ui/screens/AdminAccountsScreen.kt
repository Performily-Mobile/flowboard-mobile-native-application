package com.performily.flowboard.features.iam.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.performily.flowboard.core.designsystem.component.FlowboardTopBar
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.iam.presentation.viewmodel.IamViewModel

/**
 * Accounts of the employees for HR: change the role or reset the password of an active account.
 *
 * @param onBack called when the back button is tapped.
 * @param vm IAM view model.
 */
@Composable
fun AdminAccountsScreen(onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    val accounts by vm.accounts.collectAsState()
    val temporaryPassword by vm.temporaryPassword.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val message by vm.message.collectAsState()
    var resetCandidate by remember { mutableStateOf<Long?>(null) }
    var roleCandidate by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) { vm.loadAccounts() }

    Scaffold(
        topBar = { FlowboardTopBar(title = "Cuentas de colaboradores", onBack = onBack) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.screenHorizontal)
                .padding(top = Spacing.xs, bottom = Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            error?.let { ErrorBanner(it) }
            message?.let { SuccessBanner(it) }
            accounts.forEach { account ->
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(start = Spacing.l, end = Spacing.s, top = Spacing.m, bottom = Spacing.xs)
                    ) {
                        Text(
                            text = account.username,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${roleLabel(account.role)} · ${statusLabel(account.status)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (account.status == ACTIVE) {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                                TextButton(onClick = { roleCandidate = account.id }) { Text("Cambiar rol") }
                                TextButton(onClick = { resetCandidate = account.id }) { Text("Restablecer contraseña") }
                            }
                        }
                    }
                }
            }
        }
    }

    resetCandidate?.let { accountId ->
        AlertDialog(
            onDismissRequest = { resetCandidate = null },
            title = { Text("Restablecer contraseña") },
            text = { Text("Se generará una contraseña temporal. Debes entregarla al colaborador mediante un canal seguro.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        resetCandidate = null
                        vm.resetPassword(accountId)
                    }
                ) { Text("Restablecer") }
            },
            dismissButton = { TextButton(onClick = { resetCandidate = null }) { Text("Cancelar") } }
        )
    }

    temporaryPassword?.let { password ->
        AlertDialog(
            onDismissRequest = { vm.dismissTemporaryPassword() },
            title = { Text("Contraseña temporal") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                    Text("Muéstrala solo al colaborador correspondiente. Esta clave no se guardará en la aplicación.")
                    Text(password, style = MaterialTheme.typography.titleLarge)
                }
            },
            confirmButton = { TextButton(onClick = { vm.dismissTemporaryPassword() }) { Text("Entendido") } }
        )
    }

    roleCandidate?.let { accountId ->
        AlertDialog(
            onDismissRequest = { roleCandidate = null },
            title = { Text("Asignar rol") },
            text = { Text("Debe quedar al menos una cuenta activa de RR. HH.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.changeRole(accountId, HR_ROLE)
                        roleCandidate = null
                    }
                ) { Text("RR. HH.") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vm.changeRole(accountId, EMPLOYEE_ROLE)
                        roleCandidate = null
                    }
                ) { Text("Colaborador") }
            }
        )
    }
}

/**
 * Readable name of a role sent by the backend.
 *
 * @param role role name, for example "HR_STAFF".
 */
private fun roleLabel(role: String): String = when (role) {
    HR_ROLE -> "RR. HH."
    EMPLOYEE_ROLE -> "Colaborador"
    else -> role
}

/**
 * Readable name of an account status sent by the backend.
 *
 * @param status status name, for example "ACTIVE".
 */
private fun statusLabel(status: String): String = when (status) {
    ACTIVE -> "Activa"
    "DISABLED" -> "Deshabilitada"
    "LOCKED" -> "Bloqueada"
    else -> status
}

private const val ACTIVE = "ACTIVE"
private const val HR_ROLE = "HR_STAFF"
private const val EMPLOYEE_ROLE = "EMPLOYEE"
