package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/**
 * Chip de filtro de MA-69.
 * - clearable = false (Período, Área): siempre abre el menú; muestra la flecha.
 * - clearable = true (Estado): si está seleccionado, la "x" lo quita; si no, abre el menú.
 */
@Composable
fun <T> PayrollFilterChip(
    label: String,
    selected: Boolean,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    clearable: Boolean = false,
    onClear: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    val showClose = clearable && selected
    Box(modifier = modifier) {
        FilterChip(
            selected = selected,
            onClick = { if (showClose) onClear() else expanded = true },
            label = {
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            },
            leadingIcon = if (selected) {
                { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else {
                null
            },
            trailingIcon = {
                Icon(
                    imageVector = if (showClose) FlowboardIcons.Close else FlowboardIcons.ArrowDropDown,
                    contentDescription = if (showClose) "Quitar filtro" else null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
            )
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}
