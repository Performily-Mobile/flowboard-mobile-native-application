package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity

/**
 * State of a [CollapsingHeaderLayout]: how much of the header is currently hidden.
 *
 * @param initialOffset initial vertical offset of the header in pixels (0 means fully shown).
 */
@Stable
class CollapsingHeaderState(initialOffset: Float = 0f) {

    /** Vertical offset of the header in pixels, from minus its height (hidden) to 0 (shown). */
    var offset by mutableFloatStateOf(initialOffset)
        internal set

    /** Measured height of the header in pixels. */
    var height by mutableIntStateOf(0)
        internal set

    /** True while at least half of the header is visible; useful to expand or shrink a floating button. */
    val isHeaderVisible: Boolean
        get() = height == 0 || offset > -height / 2f

    /** Brings the header fully back into view. */
    fun show() {
        offset = 0f
    }

    internal val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            offset = (offset + available.y).coerceIn(-height.toFloat(), 0f)
            return Offset.Zero
        }
    }

    companion object {
        /** Saves only the offset, so the header keeps its position after a rotation. */
        val StateSaver: Saver<CollapsingHeaderState, Float> = Saver(
            save = { it.offset },
            restore = { CollapsingHeaderState(it) }
        )
    }
}

/**
 * Remembers a [CollapsingHeaderState] that survives configuration changes.
 */
@Composable
fun rememberCollapsingHeaderState(): CollapsingHeaderState =
    rememberSaveable(saver = CollapsingHeaderState.StateSaver) { CollapsingHeaderState() }

/**
 * Layout whose header (search box, filters, tabs) slides away while the list scrolls down and
 * comes back as soon as the user scrolls up, like the search bar of Gmail.
 *
 * The content must be scrollable (usually a LazyColumn) and must apply the given padding as its
 * content padding so its first item starts below the header.
 *
 * @param modifier modifier applied to the layout.
 * @param state state that tracks the header offset.
 * @param showHeaderKey whenever this value changes the header is shown again; pass something that
 * changes when the content may stop being scrollable (for example the number of items), so the
 * header never stays hidden over an empty or error state.
 * @param header header content; it gets the screen background.
 * @param content scrollable content; receives the padding that leaves room for the header.
 */
@Composable
fun CollapsingHeaderLayout(
    modifier: Modifier = Modifier,
    state: CollapsingHeaderState = rememberCollapsingHeaderState(),
    showHeaderKey: Any? = null,
    header: @Composable () -> Unit,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val headerHeight = with(LocalDensity.current) { state.height.toDp() }
    LaunchedEffect(showHeaderKey) { state.show() }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .nestedScroll(state.connection)
    ) {
        content(PaddingValues(top = headerHeight))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { state.height = it.height }
                .graphicsLayer { translationY = state.offset }
                .background(MaterialTheme.colorScheme.surface)
        ) {
            header()
        }
    }
}
