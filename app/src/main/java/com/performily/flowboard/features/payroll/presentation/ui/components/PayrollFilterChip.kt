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
 * Pill filter chip of the payment status screen (MA-69) that opens a menu of options.
 *
 * With `clearable = false` (period, area) a tap always opens the menu and the chip shows an arrow.
 * With `clearable = true` (status) a selected chip shows an "x" that removes the filter;
 * otherwise a tap opens the menu.
 *
 * @param label text of the chip.
 * @param selected true when the filter has a value.
 * @param options options of the menu.
 * @param optionLabel text shown for each option.
 * @param onSelect called with the option the user picks.
 * @param modifier modifier applied to the chip.
 * @param clearable true to let the user remove the filter from the chip.
 * @param onClear called when the user removes the filter.
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
                Text(text = label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium, maxLines = 1)
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
