package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.text.AppLocale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Horizontal row of filter chips that scrolls when the chips do not fit.
 *
 * The row bleeds to the screen edges and keeps the screen margin as inner padding, so the last chip
 * scrolls into view instead of being cut by the margin.
 *
 * @param modifier modifier applied to the row.
 * @param contentPadding inner padding; by default the screen side margin.
 * @param content chips of the row.
 */
@Composable
fun FlowboardChipRow(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.screenHorizontal),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
        content = content
    )
}

/**
 * Single-choice filter chip, such as "Todos", "Puntual" or "Tardanza".
 *
 * A selected chip shows a check and the secondary container color, the same as every other
 * selected filter of the app.
 *
 * @param label text of the chip.
 * @param selected whether the filter is applied.
 * @param onClick called when the chip is tapped.
 * @param modifier modifier applied to the chip.
 */
@Composable
fun FlowboardChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        leadingIcon = if (selected) {
            { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
        } else {
            null
        },
        colors = flowboardChipColors(),
        modifier = modifier
    )
}

/**
 * Filter chip that opens a menu of options.
 *
 * Without a selection it shows only [name] and an arrow. With a selection it shows "name: value";
 * when [onClear] is given, tapping it clears the filter, otherwise it opens the menu again.
 *
 * @param name name of the filter, such as "Área".
 * @param selectedLabel text of the selected option, or null when nothing is selected.
 * @param options options of the menu.
 * @param optionLabel text shown for each option.
 * @param onSelect called with the option the user picks.
 * @param modifier modifier applied to the chip.
 * @param onClear called to remove the filter, or null when the filter cannot be empty.
 */
@Composable
fun <T> FlowboardMenuChip(
    name: String,
    selectedLabel: String?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedLabel != null
    val clearable = selected && onClear != null
    Box(modifier = modifier) {
        FilterChip(
            selected = selected,
            onClick = { if (clearable) onClear?.invoke() else expanded = true },
            label = { Text(if (selected) "$name: $selectedLabel" else name, maxLines = 1) },
            leadingIcon = if (selected) {
                { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
            } else {
                null
            },
            trailingIcon = {
                Icon(
                    imageVector = if (clearable) FlowboardIcons.Close else FlowboardIcons.ArrowDropDown,
                    contentDescription = if (clearable) "Quitar filtro" else null,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = flowboardChipColors()
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

/**
 * Chip that shows a period, such as "1 – 30 nov 2026", and opens a range calendar to change it.
 *
 * It replaces a pair of "Desde" and "Hasta" fields: one control, a third of the height, and the
 * calendar never lets the end come before the start.
 *
 * @param from first day of the period.
 * @param to last day of the period.
 * @param onRangeChange called with the new first and last day.
 * @param modifier modifier applied to the chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowboardDateRangeChip(
    from: LocalDate,
    to: LocalDate,
    onRangeChange: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    FilterChip(
        selected = true,
        onClick = { showPicker = true },
        label = { Text(formatRange(from, to), maxLines = 1) },
        leadingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
        colors = flowboardChipColors(),
        modifier = modifier
    )
    if (showPicker) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = from.toEpochMillis(),
            initialSelectedEndDateMillis = to.toEpochMillis()
        )
        val canConfirm by remember {
            derivedStateOf { state.selectedStartDateMillis != null && state.selectedEndDateMillis != null }
        }
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    enabled = canConfirm,
                    onClick = {
                        val start = state.selectedStartDateMillis
                        val end = state.selectedEndDateMillis
                        if (start != null && end != null) onRangeChange(start.toLocalDate(), end.toLocalDate())
                        showPicker = false
                    }
                ) { Text("Aplicar") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancelar") } }
        ) {
            DateRangePicker(
                state = state,
                title = {
                    Text(
                        text = "Selecciona el período",
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                    )
                },
                showModeToggle = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/** Colors shared by every filter chip: secondary container when selected. */
@Composable
private fun flowboardChipColors(): SelectableChipColors = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
    selectedTrailingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
)

private val DayMonthYear: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", AppLocale)
private val DayMonth: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", AppLocale)

/**
 * Short text of a period that does not repeat the month or the year when both days share them.
 *
 * @param from first day.
 * @param to last day.
 * @return text such as "1 – 30 nov 2026", "28 oct – 3 nov 2026" or "15 dic 2026 – 10 ene 2027".
 */
private fun formatRange(from: LocalDate, to: LocalDate): String {
    val text = when {
        from == to -> from.format(DayMonthYear)
        from.year != to.year -> "${from.format(DayMonthYear)} – ${to.format(DayMonthYear)}"
        from.month != to.month -> "${from.format(DayMonth)} – ${to.format(DayMonthYear)}"
        else -> "${from.dayOfMonth} – ${to.format(DayMonthYear)}"
    }
    return text.replace(".", "")
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
