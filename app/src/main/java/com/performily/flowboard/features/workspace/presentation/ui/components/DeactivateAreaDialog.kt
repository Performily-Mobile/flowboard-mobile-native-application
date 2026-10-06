package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Area

@Composable
fun DeactivateAreaDialog(
    area: Area,
    confirmed: Boolean,
    isSaving: Boolean,
    errorMessage: String?,
    onConfirmedChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val canDeactivate = area.canBeDeactivated
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(FlowboardIcons.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Desactivar el área ${area.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (canDeactivate) {
                        "Esta acción no se puede deshacer desde la aplicación. El área dejará de aparecer al crear " +
                            "o editar posiciones. No tiene colaboradores activos, así que nadie queda sin área."
                    } else {
                        "El área tiene ${area.activeEmployees} colaboradores activos. Reasígnalos a otra área " +
                            "antes de desactivarla."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (canDeactivate) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(value = confirmed, role = Role.Checkbox, onValueChange = onConfirmedChange),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = confirmed, onCheckedChange = null)
                        Text(
                            text = "Entiendo el efecto de esta acción y quiero continuar.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            if (canDeactivate) {
                Button(
                    onClick = onConfirm,
                    enabled = confirmed && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) { Text("Desactivar área") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(if (canDeactivate) "Cancelar" else "Cerrar") }
        }
    )
}
