package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * MA-75 · Campo "Rango de fechas". Abre un selector de rango que no deja elegir
 * días futuros (el histórico solo tiene lecturas pasadas).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeField(
    from: LocalDate,
    to: LocalDate,
    onRangeChange: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now()
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = WellbeingFormatters.dateRange(from, to),
            onValueChange = {},
            readOnly = true,
            label = { Text("Rango de fechas") },
            trailingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        val todayMillis = today.toEpochMillis()
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = from.toEpochMillis(),
            initialSelectedEndDateMillis = to.toEpochMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayMillis
                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    enabled = state.selectedStartDateMillis != null,
                    onClick = {
                        val start = state.selectedStartDateMillis?.toLocalDate()
                        // Si solo se eligió un día, el rango es ese mismo día.
                        val end = state.selectedEndDateMillis?.toLocalDate() ?: start
                        if (start != null && end != null) onRangeChange(start, end)
                        showDialog = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        ) {
            DateRangePicker(
                state = state,
                title = { Text("Selecciona el rango", modifier = Modifier) },
                modifier = Modifier.height(480.dp)
            )
        }
    }
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
