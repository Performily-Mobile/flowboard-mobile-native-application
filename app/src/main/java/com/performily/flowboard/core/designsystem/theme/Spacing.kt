package com.performily.flowboard.core.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Spacing scale of the app.
 *
 * Screens use [screenHorizontal] as side margin, [sectionGap] between blocks and [itemGap]
 * between the items of a list, so every view breathes the same way.
 */
object Spacing {
    val xs = 4.dp
    val s = 8.dp
    val m = 12.dp
    val l = 16.dp
    val xl = 24.dp
    val xxl = 32.dp

    /** Left and right margin of every screen. */
    val screenHorizontal = 20.dp

    /** Gap between independent blocks of a screen. */
    val sectionGap = 24.dp

    /** Gap between the cards or rows of a list. */
    val itemGap = 12.dp

    /** Bottom space of a list so its last item is not covered by a floating button. */
    val fabClearance = 96.dp
}
