package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.request.domain.entity.RequestField
import com.performily.flowboard.features.request.domain.valueobject.FieldDataType
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset


@Composable
fun <T> RequestSelectField(
    label: String,
    options: List<T>,
    selected: T?,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    trailingIcon = if (option == selected) {
                        { Icon(FlowboardIcons.Check, contentDescription = null) }
                    } else {
                        null
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDateField(
    label: String,
    value: LocalDate?,
    onValueChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    minDate: LocalDate? = null,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value?.let(RequestFormatters::date).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
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
        val minMillis = minDate?.toEpochMillis()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (value ?: minDate ?: LocalDate.now()).toEpochMillis(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = minMillis == null || utcTimeMillis >= minMillis
                override fun isSelectableYear(year: Int): Boolean = minDate == null || year >= minDate.year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { onValueChange(it.toLocalDate()) }
                        showDialog = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestTimeField(
    label: String,
    value: LocalTime?,
    onValueChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value?.let(RequestFormatters::time).orEmpty(),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { Icon(FlowboardIcons.Schedule, contentDescription = null) },
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } },
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
        val initial = value ?: LocalTime.of(9, 0)
        val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(label) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onValueChange(LocalTime.of(state.hour, state.minute))
                        showDialog = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancelar") }
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> RequestSegmented(
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = option == selected,
                onClick = { onSelect(option) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
            ) {
                Text(optionLabel(option), maxLines = 1)
            }
        }
    }
}


@Composable
fun DynamicFieldInput(
    field: RequestField,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val label = if (field.required) field.label else "${field.label} (opcional)"
    when (field.dataType) {
        FieldDataType.DATE -> RequestDateField(
            label = label,
            value = runCatching { LocalDate.parse(value) }.getOrNull(),
            onValueChange = { onValueChange(it.toString()) },
            isError = error != null,
            supportingText = error,
            modifier = modifier.fillMaxWidth()
        )
        FieldDataType.TIME -> RequestTimeField(
            label = label,
            value = runCatching { LocalTime.parse(value) }.getOrNull(),
            onValueChange = { onValueChange(RequestFormatters.time(it)) },
            isError = error != null,
            supportingText = error,
            modifier = modifier.fillMaxWidth()
        )
        FieldDataType.BOOLEAN -> Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(
                checked = value.equals("true", ignoreCase = true),
                onCheckedChange = { onValueChange(it.toString()) }
            )
        }
        FieldDataType.NUMBER, FieldDataType.MONEY -> OutlinedTextField(
            value = value,
            onValueChange = { text -> onValueChange(text.filter { it.isDigit() || it == '.' || it == '-' }) },
            label = { Text(label) },
            prefix = if (field.dataType == FieldDataType.MONEY) {
                { Text("S/ ") }
            } else {
                null
            },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = modifier.fillMaxWidth()
        )
        FieldDataType.TEXT -> OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it.take(MAX_TEXT)) },
            label = { Text(label) },
            isError = error != null,
            supportingText = error?.let { { Text(it) } },
            minLines = 3,
            modifier = modifier.fillMaxWidth()
        )
    }
}

private const val MAX_TEXT = 500

private fun LocalDate.toEpochMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
