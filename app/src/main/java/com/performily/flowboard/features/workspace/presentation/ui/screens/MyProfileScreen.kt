package com.performily.flowboard.features.workspace.presentation.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.workspace.domain.entity.Employee
import com.performily.flowboard.features.workspace.domain.valueobject.IdentityDocument
import com.performily.flowboard.features.workspace.presentation.ui.components.DetailRow
import com.performily.flowboard.features.workspace.presentation.ui.components.EmployeeAvatar
import com.performily.flowboard.features.workspace.presentation.ui.components.SectionTitle
import com.performily.flowboard.features.workspace.presentation.ui.components.StatusChip
import com.performily.flowboard.features.workspace.presentation.ui.components.label
import com.performily.flowboard.features.workspace.presentation.ui.components.toDisplay
import com.performily.flowboard.features.workspace.presentation.viewmodel.MyProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit,
    onMyRecordClick: () -> Unit,
    onOrganizationChartClick: (areaId: Long, employeeId: Long) -> Unit,
    viewModel: MyProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(FlowboardIcons.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        val employee = state.employee
        when {
            state.isLoading && employee == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            employee == null -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(state.errorMessage ?: "No se pudo cargar tu perfil.")
                OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
            }

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ProfileCard(employee)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShortcutButton(
                        text = "Mi expediente",
                        icon = FlowboardIcons.Folder,
                        onClick = onMyRecordClick,
                        modifier = Modifier.weight(1f)
                    )
                    ShortcutButton(
                        text = "Organigrama",
                        icon = FlowboardIcons.AccountTree,
                        onClick = { onOrganizationChartClick(employee.areaId, employee.id.value) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Column {
                    SectionTitle("Datos laborales")
                    DetailRow(
                        label = "Jefe directo",
                        value = state.directManager?.let { "${it.name.fullName} · ${it.positionTitle}" }
                            ?: "Sin jefe directo"
                    )
                    DetailRow(label = "Fecha de ingreso", value = employee.employmentPeriod.hireDate.toDisplay())
                    LockedRow(label = "Sueldo asignado", value = "S/ ••••••")

                    SectionTitle("Datos personales")
                    DetailRow(
                        label = "Documento de identidad",
                        value = employee.identityDocument.masked()
                    )
                    DetailRow(label = "Correo electrónico", value = employee.email.value)
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(employee: Employee) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Divider)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            EmployeeAvatar(initials = employee.name.initials, size = 72.dp)
            Text(text = employee.name.fullName, style = MaterialTheme.typography.titleLarge)
            Text(
                text = employee.jobDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StatusChip(status = employee.status)
        }
    }
}

@Composable
private fun ShortcutButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(onClick = onClick, shape = RoundedCornerShape(8.dp), modifier = modifier) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Text(text, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun LockedRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = value, style = MaterialTheme.typography.bodyLarge)
            }
            Icon(
                imageVector = FlowboardIcons.Lock,
                contentDescription = "Dato protegido",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        HorizontalDivider(color = Divider)
    }
}

/** "DNI 4•••••30": solo se muestran el primer dígito y los dos últimos. */
private fun IdentityDocument.masked(): String {
    val visible = if (number.length > 3) {
        number.first() + "•".repeat(number.length - 3) + number.takeLast(2)
    } else {
        number
    }
    return "${type.label()} $visible"
}
