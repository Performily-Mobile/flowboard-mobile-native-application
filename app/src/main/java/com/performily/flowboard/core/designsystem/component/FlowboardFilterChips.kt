package com.performily.flowboard.core.designsystem.component

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.core.text.AppLocale
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.TextStyle

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
 * Chip that opens a menu of options.
 *
 * It works in two ways:
 * - Optional filter (with [onClear]): empty it shows [name] and an arrow; applied it shows a check,
 *   "name: value" and an "x" that removes the filter.
 * - Required selector (without [onClear]), such as the period or the area of a report: it always has
 *   a value, so it shows [icon] instead of a check and keeps the arrow to change the value.
 *
 * @param name name of the filter, such as "Área".
 * @param selectedLabel text of the selected option, or null when nothing is selected.
 * @param options options of the menu.
 * @param optionLabel text shown for each option.
 * @param onSelect called with the option the user picks.
 * @param modifier modifier applied to the chip.
 * @param onClear called to remove the filter, or null for a required selector.
 * @param icon optional leading icon of a required selector.
 * @param showNameWithValue false to show only the value once selected, for self-explanatory values such as "Octubre 2026".
 * @param enabled false to show the chip without reacting to taps.
 */
@Composable
fun <T> FlowboardMenuChip(
    name: String,
    selectedLabel: String?,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
    icon: ImageVector? = null,
    showNameWithValue: Boolean = true,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = selectedLabel != null
    val clearable = selected && onClear != null
    val text = when {
        !selected -> name
        showNameWithValue -> "$name: $selectedLabel"
        else -> selectedLabel.orEmpty()
    }
    Box(modifier = modifier) {
        FilterChip(
            selected = selected,
            enabled = enabled,
            onClick = { if (clearable) onClear?.invoke() else expanded = true },
            label = { Text(text, maxLines = 1) },
            leadingIcon = when {
                clearable -> {
                    { Icon(FlowboardIcons.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                }
                icon != null -> {
                    { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
                }
                else -> null
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
 * Chip that shows a period, such as "1 – 30 nov 2026", with a menu of common periods
 * ("Este mes", "Mes anterior", "Últimos 30 días"…) and an "Elegir fechas…" option that opens the
 * range calendar for any other period.
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
    var showMenu by remember { mutableStateOf(false) }
    var showPicker by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = true,
            onClick = { showMenu = true },
            label = { Text(formatRange(from, to), maxLines = 1) },
            leadingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = { Icon(FlowboardIcons.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp)) },
            colors = flowboardChipColors()
        )
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            val today = LocalDate.now()
            PeriodPreset.entries.forEach { preset ->
                val (start, end) = preset.range(today)
                DropdownMenuItem(
                    text = { Text(preset.label) },
                    trailingIcon = if (start == from && end == to) {
                        { Icon(FlowboardIcons.Check, contentDescription = "Seleccionado", modifier = Modifier.size(18.dp)) }
                    } else {
                        null
                    },
                    onClick = {
                        showMenu = false
                        onRangeChange(start, end)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text("Elegir fechas…") },
                leadingIcon = { Icon(FlowboardIcons.Calendar, contentDescription = null, modifier = Modifier.size(18.dp)) },
                onClick = {
                    showMenu = false
                    showPicker = true
                }
            )
        }
    }
    if (showPicker) {
        DateRangePickerScreen(
            from = from,
            to = to,
            onDismiss = { showPicker = false },
            onConfirm = { start, end ->
                onRangeChange(start, end)
                showPicker = false
            }
        )
    }
}

/**
 * Full-screen range calendar, the Material 3 pattern for picking a period on a phone.
 *
 * A range needs the whole screen to show the selection across weeks; inside a small dialog the
 * month grid and the header get squeezed. Months scroll vertically, and the pencil icon switches to
 * typing both dates, which is faster for periods far from the current month. The calendar reads its language from the configuration,
 * so it receives a copy set to [AppLocale] and shows month and weekday names in Spanish even when
 * the device is in another language.
 *
 * @param from first day selected when the calendar opens.
 * @param to last day selected when the calendar opens.
 * @param onDismiss called when the user closes the calendar without applying.
 * @param onConfirm called with the first and last day when the user taps "Aplicar".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangePickerScreen(
    from: LocalDate,
    to: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, LocalDate) -> Unit
) {
    val baseConfiguration = LocalConfiguration.current
    val spanishConfiguration = remember(baseConfiguration) {
        Configuration(baseConfiguration).apply { setLocale(AppLocale) }
    }
    CompositionLocalProvider(LocalConfiguration provides spanishConfiguration) {
        val state = rememberDateRangePickerState(
            initialSelectedStartDateMillis = from.toEpochMillis(),
            initialSelectedEndDateMillis = to.toEpochMillis()
        )
        val canConfirm by remember {
            derivedStateOf { state.selectedStartDateMillis != null && state.selectedEndDateMillis != null }
        }
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(horizontal = Spacing.xs, vertical = Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(FlowboardIcons.Close, contentDescription = "Cerrar")
                        }
                        Text(
                            text = "Período",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            enabled = canConfirm,
                            onClick = {
                                val start = state.selectedStartDateMillis
                                val end = state.selectedEndDateMillis
                                if (start != null && end != null) onConfirm(start.toLocalDate(), end.toLocalDate())
                            }
                        ) { Text("Aplicar") }
                    }
                    DateRangePicker(
                        state = state,
                        title = {
                            Text(
                                text = "Selecciona el inicio y el fin",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = Spacing.s)
                            )
                        },
                        headline = {
                            Text(
                                text = rangeHeadline(state.selectedStartDateMillis, state.selectedEndDateMillis),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = Spacing.m)
                            )
                        },
                        showModeToggle = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Headline of the range calendar while the user picks the days.
 *
 * @param start first selected day, or null.
 * @param end last selected day, or null.
 * @return the formatted range, or a hint of the next day to pick.
 */
private fun rangeHeadline(start: Long?, end: Long?): String = when {
    start == null -> "Elige el primer día"
    end == null -> "${formatDay(start.toLocalDate(), withYear = true)} – elige el último día"
    else -> formatRange(start.toLocalDate(), end.toLocalDate())
}

/**
 * Common periods offered by [FlowboardDateRangeChip], so most changes take one tap instead of
 * scrolling the calendar.
 *
 * @property label text of the option.
 */
private enum class PeriodPreset(val label: String) {
    THIS_MONTH("Este mes"),
    LAST_MONTH("Mes anterior"),
    LAST_7_DAYS("Últimos 7 días"),
    LAST_30_DAYS("Últimos 30 días"),
    LAST_3_MONTHS("Últimos 3 meses");

    /**
     * First and last day of the period, counted from [today].
     *
     * @param today current day.
     * @return the first and the last day.
     */
    fun range(today: LocalDate): Pair<LocalDate, LocalDate> = when (this) {
        THIS_MONTH -> today.withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
        LAST_MONTH -> today.minusMonths(1).let { it.withDayOfMonth(1) to it.withDayOfMonth(it.lengthOfMonth()) }
        LAST_7_DAYS -> today.minusDays(6) to today
        LAST_30_DAYS -> today.minusDays(29) to today
        LAST_3_MONTHS -> today.minusMonths(2).withDayOfMonth(1) to today.withDayOfMonth(today.lengthOfMonth())
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

/**
 * Day and short month in Spanish, in lower case and without the period, such as "2 nov" or "2 nov 2026".
 *
 * @param date day to format.
 * @param withYear true to add the year.
 * @return the formatted day.
 */
private fun formatDay(date: LocalDate, withYear: Boolean): String {
    val month = date.month.getDisplayName(TextStyle.SHORT, AppLocale).lowercase(AppLocale).replace(".", "")
    return if (withYear) "${date.dayOfMonth} $month ${date.year}" else "${date.dayOfMonth} $month"
}

/**
 * Short text of a period that does not repeat the month or the year when both days share them.
 *
 * @param from first day.
 * @param to last day.
 * @return text such as "1 – 30 nov 2026", "28 oct – 3 nov 2026" or "15 dic 2026 – 10 ene 2027".
 */
private fun formatRange(from: LocalDate, to: LocalDate): String = when {
    from == to -> formatDay(from, withYear = true)
    from.year != to.year -> "${formatDay(from, withYear = true)} – ${formatDay(to, withYear = true)}"
    from.month != to.month -> "${formatDay(from, withYear = false)} – ${formatDay(to, withYear = true)}"
    else -> "${from.dayOfMonth} – ${formatDay(to, withYear = true)}"
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()
