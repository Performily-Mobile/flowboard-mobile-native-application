package com.performily.flowboard.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Pill-shaped tab selector used for every set of tabs and every single-choice switch of the app.
 *
 * Up to three short options share the width equally; with more options or longer labels the row
 * scrolls horizontally so no label gets cut.
 *
 * @param options options to show, in order.
 * @param selected option currently selected.
 * @param onSelect called with the option the user taps.
 * @param label text shown for each option.
 * @param modifier modifier applied to the selector.
 * @param enabled false to show the selector without reacting to taps.
 */
@Composable
fun <T> FlowboardTabs(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val labels = options.map(label)
    val fillWidth = options.size <= MAX_EQUAL_WIDTH_OPTIONS && labels.all { it.length <= MAX_EQUAL_WIDTH_LABEL }
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (fillWidth) Modifier else Modifier.horizontalScroll(rememberScrollState()))
                .padding(4.dp)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                TabPill(
                    text = labels[index],
                    selected = option == selected,
                    enabled = enabled,
                    fillWidth = fillWidth,
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

/**
 * Index-based version of [FlowboardTabs] for screens that keep the selected tab as a number.
 *
 * @param labels text of each tab.
 * @param selectedIndex position of the selected tab.
 * @param onSelect called with the position the user taps.
 * @param modifier modifier applied to the selector.
 */
@Composable
fun FlowboardTabs(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardTabs(
        options = labels.indices.toList(),
        selected = selectedIndex,
        onSelect = onSelect,
        label = { labels[it] },
        modifier = modifier
    )
}

/** One option of the selector: a pill that turns into a raised chip when selected. */
@Composable
private fun RowScope.TabPill(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    fillWidth: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceContainerHigh,
        label = "tabBackground"
    )
    val content by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabContent"
    )
    Box(
        modifier = Modifier
            .then(if (fillWidth) Modifier.weight(1f) else Modifier)
            .height(40.dp)
            .then(if (selected) Modifier.shadow(1.dp, CircleShape) else Modifier)
            .clip(CircleShape)
            .background(background)
            .selectable(selected = selected, enabled = enabled, role = Role.Tab, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = content,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private const val MAX_EQUAL_WIDTH_OPTIONS = 3
private const val MAX_EQUAL_WIDTH_LABEL = 12
