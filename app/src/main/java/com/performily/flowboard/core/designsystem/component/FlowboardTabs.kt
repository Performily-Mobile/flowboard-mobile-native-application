package com.performily.flowboard.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

/**
 * Pill-shaped tab selector used for every set of tabs and every single-choice switch of the app.
 *
 * The pills always fill the whole track: when their labels fit they share the width (equally when
 * every label fits in an equal share, otherwise each one keeps its natural width plus an equal part
 * of the free space), and only when they do not fit the row scrolls horizontally, so a label is
 * never cut and the track never shows an empty gap.
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
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val trackWidth = if (constraints.hasBoundedWidth) constraints.maxWidth else null
            Layout(
                content = {
                    options.forEach { option ->
                        TabPill(
                            text = label(option),
                            selected = option == selected,
                            enabled = enabled,
                            onClick = { onSelect(option) }
                        )
                    }
                },
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(TRACK_PADDING)
                    .selectableGroup()
            ) { measurables, _ ->
                val gap = PILL_GAP.roundToPx()
                val pillHeight = PILL_HEIGHT.roundToPx()
                val gaps = gap * (measurables.size - 1).coerceAtLeast(0)
                val naturalWidths = measurables.map { it.maxIntrinsicWidth(pillHeight) }
                val space = trackWidth?.let { it - TRACK_PADDING.roundToPx() * 2 - gaps }
                val widths = if (space == null) naturalWidths else tabWidths(naturalWidths, space)
                val placeables = measurables.mapIndexed { index, measurable ->
                    measurable.measure(Constraints.fixed(widths[index], pillHeight))
                }
                layout(width = (widths.sum() + gaps).coerceAtLeast(0), height = pillHeight) {
                    var x = 0
                    placeables.forEach { placeable ->
                        placeable.placeRelative(x, 0)
                        x += placeable.width + gap
                    }
                }
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

/**
 * Width of each pill so the pills cover the whole track.
 *
 * @param naturalWidths width each pill needs to show its label.
 * @param space width available for the pills, without the gaps between them.
 * @return the natural widths when they do not fit (the row then scrolls); otherwise equal widths
 * when every label fits in an equal share, or the natural widths plus an equal part of the free space.
 */
internal fun tabWidths(naturalWidths: List<Int>, space: Int): List<Int> {
    if (naturalWidths.isEmpty()) return emptyList()
    val total = naturalWidths.sum()
    if (total >= space) return naturalWidths
    val count = naturalWidths.size
    val equalShare = space / count
    val widths = if (naturalWidths.all { it <= equalShare }) {
        List(count) { equalShare }
    } else {
        val extra = (space - total) / count
        naturalWidths.map { it + extra }
    }
    val leftover = space - widths.sum()
    return widths.mapIndexed { index, width -> if (index == count - 1) width + leftover else width }
}

/** One option of the selector: a pill that turns into a raised chip when selected. */
@Composable
private fun TabPill(
    text: String,
    selected: Boolean,
    enabled: Boolean,
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
            softWrap = false
        )
    }
}

/** Space between the track and the pills. */
private val TRACK_PADDING = 4.dp

/** Space between two pills. */
private val PILL_GAP = 4.dp

/** Height of each pill. */
private val PILL_HEIGHT = 40.dp
