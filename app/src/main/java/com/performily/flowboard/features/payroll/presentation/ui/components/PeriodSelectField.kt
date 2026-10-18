package com.performily.flowboard.features.payroll.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.payroll.domain.entity.PayrollPeriod

/**
 * "Periodo" field that opens the list of payroll periods (MA-67).
 *
 * @param periods payroll periods to choose from.
 * @param selected period currently selected, or null.
 * @param onSelect called with the period the user picks.
 * @param modifier modifier applied to the field.
 * @param enabled false to block the field, for example during an upload.
 */
@Composable
fun PeriodSelectField(
    periods: List<PayrollPeriod>,
    selected: PayrollPeriod?,
    onSelect: (PayrollPeriod) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected?.period?.label.orEmpty(),
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Periodo") },
            trailingIcon = {
                Icon(FlowboardIcons.ArrowDropDown, contentDescription = null, modifier = Modifier.size(22.dp))
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(enabled = enabled && periods.isNotEmpty()) { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            periods.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.period.label) },
                    onClick = {
                        expanded = false
                        onSelect(period)
                    }
                )
            }
        }
    }
}
