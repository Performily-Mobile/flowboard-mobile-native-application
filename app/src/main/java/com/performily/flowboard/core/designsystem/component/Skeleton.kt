package com.performily.flowboard.core.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Loading placeholder that copies the shape of the content that is about to appear.
 *
 * The blocks are flat and pulse softly in opacity (no shimmer). Screen readers hear a single
 * "Cargando" instead of the blocks.
 *
 * @param modifier modifier applied to the placeholder.
 * @param label text read by the screen reader.
 * @param content blocks of the placeholder.
 */
@Composable
fun SkeletonPulse(
    modifier: Modifier = Modifier,
    label: String = "Cargando",
    content: @Composable ColumnScope.() -> Unit
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    Column(
        modifier = modifier
            .clipToBounds()
            .clearAndSetSemantics {
                contentDescription = label
                liveRegion = LiveRegionMode.Polite
            }
            .alpha(alpha),
        content = content
    )
}

/**
 * Flat rounded block of a skeleton.
 *
 * @param height height of the block.
 * @param modifier modifier applied to the block; without a width it fills the available width.
 * @param width optional fixed width.
 * @param radius corner radius.
 */
@Composable
fun SkeletonBox(
    height: Dp,
    modifier: Modifier = Modifier,
    width: Dp? = null,
    radius: Dp = 6.dp
) {
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .background(skeletonColor(), RoundedCornerShape(radius))
    )
}

/**
 * Round block of a skeleton, used for avatars.
 *
 * @param size diameter of the circle.
 */
@Composable
fun SkeletonCircle(size: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(skeletonColor(), RoundedCornerShape(percent = 50))
    )
}

/**
 * Placeholder of a list row: avatar, two lines of text and a status chip.
 *
 * @param showAvatar true when the real row starts with an avatar or an icon.
 * @param showTrailing true when the real row ends with a chip.
 */
@Composable
fun SkeletonListTile(showAvatar: Boolean = true, showTrailing: Boolean = true) {
    SkeletonCard(padding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showAvatar) SkeletonCircle()
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SkeletonBox(height = 14.dp, modifier = Modifier.fillMaxWidth(0.7f))
                SkeletonBox(height = 12.dp, modifier = Modifier.fillMaxWidth(0.45f))
            }
            if (showTrailing) SkeletonBox(height = 24.dp, width = 64.dp, radius = 999.dp)
        }
    }
}

/**
 * Placeholder of a whole list. It does not scroll, so it can fill a screen or be placed as one
 * item of a lazy list.
 *
 * @param modifier modifier applied to the list.
 * @param itemCount number of rows.
 * @param contentPadding outer padding, equal to the real list.
 * @param showAvatar true when the real rows start with an avatar or an icon.
 * @param showTrailing true when the real rows end with a chip.
 */
@Composable
fun SkeletonList(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
    contentPadding: PaddingValues = PaddingValues(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
    showAvatar: Boolean = true,
    showTrailing: Boolean = true
) {
    SkeletonPulse(modifier = modifier) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
        ) {
            repeat(itemCount) { SkeletonListTile(showAvatar = showAvatar, showTrailing = showTrailing) }
        }
    }
}

/**
 * Placeholder of a record screen: header with avatar, an optional tab bar and blocks of label
 * and value rows.
 *
 * @param modifier modifier applied to the placeholder.
 * @param showTabs true when the real screen has a tab bar.
 */
@Composable
fun SkeletonDetail(modifier: Modifier = Modifier, showTabs: Boolean = true) {
    SkeletonPulse(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.m,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.l),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SkeletonCircle(size = 72.dp)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SkeletonBox(height = 20.dp, modifier = Modifier.fillMaxWidth(0.75f))
                    SkeletonBox(height = 14.dp, modifier = Modifier.fillMaxWidth(0.5f))
                    SkeletonBox(height = 24.dp, width = 72.dp, radius = 999.dp)
                }
            }
            if (showTabs) SkeletonBox(height = 48.dp, radius = 999.dp)
            repeat(2) {
                SkeletonCard(padding = PaddingValues(Spacing.l)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
                        repeat(4) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                SkeletonBox(height = 11.dp, modifier = Modifier.fillMaxWidth(0.3f))
                                SkeletonBox(height = 15.dp, modifier = Modifier.fillMaxWidth(0.65f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Placeholder of a form: fields stacked with the form spacing.
 *
 * @param modifier modifier applied to the placeholder.
 * @param fieldCount number of fields.
 */
@Composable
fun SkeletonForm(modifier: Modifier = Modifier, fieldCount: Int = 6) {
    SkeletonPulse(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.s,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.l)
        ) {
            repeat(fieldCount) { SkeletonBox(height = 56.dp, radius = 12.dp) }
        }
    }
}

/**
 * Placeholder of a dashboard: four indicator cards and two section cards.
 *
 * @param modifier modifier applied to the placeholder.
 */
@Composable
fun SkeletonDashboard(modifier: Modifier = Modifier) {
    SkeletonPulse(modifier = modifier) {
        Column(
            modifier = Modifier.padding(
                start = Spacing.screenHorizontal,
                end = Spacing.screenHorizontal,
                top = Spacing.s,
                bottom = Spacing.xl
            ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.m)) {
                repeat(2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
                        repeat(2) {
                            Box(modifier = Modifier.weight(1f)) {
                                SkeletonCard(padding = PaddingValues(Spacing.l)) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        SkeletonCircle(size = 36.dp)
                                        SkeletonBox(height = 28.dp, width = 48.dp)
                                        SkeletonBox(height = 14.dp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            repeat(2) {
                SkeletonCard(padding = PaddingValues(Spacing.l)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.l)) {
                        SkeletonBox(height = 18.dp, width = 160.dp)
                        repeat(3) { SkeletonBox(height = 14.dp) }
                    }
                }
            }
        }
    }
}

/** Card that holds the blocks of a placeholder, with the shape and border of the real cards. */
@Composable
private fun SkeletonCard(padding: PaddingValues, content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

/** Color of the blocks, softer in the dark theme. */
@Composable
private fun skeletonColor(): Color =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color(0x1AFFFFFF) else Color(0xFFE6E8EF)
