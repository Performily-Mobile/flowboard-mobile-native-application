package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.wellbeing.domain.entity.Device

/**
 * MA-73 - Link device.
 *
 * The user types the code or taps an inventory device to fill it in. The backend error
 * (unknown code or already linked) is shown below the field.
 *
 * @param officeName name of the office the device will be assigned to
 * @param code typed device code
 * @param codeError error of the last link attempt
 * @param inventory devices available in the inventory
 * @param inventoryError error while loading the inventory, shown instead of the empty message
 * @param isLoadingInventory true while the inventory is loading
 * @param isLinking true while the link request is running
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkDeviceSheet(
    officeName: String,
    code: String,
    codeError: String?,
    inventory: List<Device>,
    inventoryError: String?,
    isLoadingInventory: Boolean,
    isLinking: Boolean,
    onCodeChange: (String) -> Unit,
    onLink: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Vincular dispositivo", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Se asignará a $officeName. Solo puedes vincular dispositivos que estén en el inventario.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = code,
                onValueChange = onCodeChange,
                label = { Text("Código del dispositivo") },
                placeholder = { Text("Ej. SNS-0042") },
                isError = codeError != null,
                supportingText = codeError?.let { { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Disponibles en inventario",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            when {
                isLoadingInventory -> CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.CenterHorizontally),
                    strokeWidth = 2.dp
                )

                inventoryError != null -> Text(
                    text = inventoryError,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )

                inventory.isEmpty() -> Text(
                    text = "No hay dispositivos disponibles en el inventario.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                else -> Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    inventory.forEach { device ->
                        DeviceRow(
                            device = device,
                            showLastReading = false,
                            onClick = { onCodeChange(device.code) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Button(
                    onClick = onLink,
                    enabled = !isLinking && code.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLinking) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Vincular")
                    }
                }
            }
        }
    }
}
