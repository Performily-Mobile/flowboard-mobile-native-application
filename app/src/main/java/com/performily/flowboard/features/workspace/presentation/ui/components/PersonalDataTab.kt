package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.workspace.domain.entity.Employee

@Composable
fun PersonalDataTab(
    employee: Employee,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        DetailRow(
            label = "Documento de identidad",
            value = "${employee.identityDocument.type.label()} ${employee.identityDocument.number}"
        )
        DetailRow(label = "Fecha de nacimiento", value = employee.birthDate.value.toDisplay())
        DetailRow(label = "Correo corporativo", value = employee.email.value)
        DetailRow(label = "Teléfono", value = employee.phoneNumber.value)
        DetailRow(label = "Dirección", value = employee.address.formatted)
        employee.updatedAt?.let { updatedAt ->
            Text(
                text = "Última modificación: ${updatedAt.toDisplay()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
