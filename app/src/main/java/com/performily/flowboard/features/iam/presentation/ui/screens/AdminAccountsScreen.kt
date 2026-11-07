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
fun AdminAccountsScreen(onBack: () -> Unit, vm: IamViewModel = hiltViewModel()) {
    val accounts by vm.accounts.collectAsState()
    val temporaryPassword by vm.temporaryPassword.collectAsState()
    var resetCandidate by remember { mutableStateOf<Long?>(null) }
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
                        TextButton(onClick = { resetCandidate = a.id }) { Text("Restablecer contraseña") }
                    }
                }
            }
        }
    }
    if (resetCandidate != null) AlertDialog(
        onDismissRequest = { resetCandidate = null },
        title = { Text("Restablecer contraseña") },
        text = { Text("Se generará una contraseña temporal. Debes entregarla al colaborador mediante un canal seguro.") },
        confirmButton = { TextButton(onClick = { val id = resetCandidate!!; resetCandidate = null; vm.resetPassword(id) }) { Text("Restablecer") } },
        dismissButton = { TextButton(onClick = { resetCandidate = null }) { Text("Cancelar") } }
    )
    if (temporaryPassword != null) AlertDialog(
        onDismissRequest = { vm.dismissTemporaryPassword() },
        title = { Text("Contraseña temporal") },
        text = { Column { Text("Muéstrala solo al colaborador correspondiente. Esta clave no se guardará en la aplicación."); Spacer(Modifier.height(12.dp)); Text(temporaryPassword.orEmpty(), style = MaterialTheme.typography.titleLarge) } },
        confirmButton = { TextButton(onClick = { vm.dismissTemporaryPassword() }) { Text("Entendido") } }
    )
    if (selected != null) AlertDialog(onDismissRequest = { selected = null }, title = { Text("Asignar rol") }, text = { Text("El backend validará que exista al menos una cuenta activa de RR.HH.") }, confirmButton = { TextButton(onClick = { vm.changeRole(selected!!, "HR_STAFF"); selected = null }) { Text("RR.HH.") } }, dismissButton = { TextButton(onClick = { vm.changeRole(selected!!, "EMPLOYEE"); selected = null }) { Text("Colaborador") } })
}

