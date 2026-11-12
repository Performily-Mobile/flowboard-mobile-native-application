package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Primary

/**
 * Flowboard logo: a blue rounded square with three white bars, the same drawing as the app icon.
 *
 * @param modifier modifier applied to the logo.
 * @param size width and height of the logo.
 */
@Composable
fun FlowboardLogo(modifier: Modifier = Modifier, size: Dp = 24.dp) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.width / 30f
        drawRoundRect(color = Primary, cornerRadius = CornerRadius(9 * unit))
        LOGO_BARS.forEach { (x, y, width) ->
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(x * unit, y * unit),
                size = Size(width * unit, 2.3f * unit),
                cornerRadius = CornerRadius(1.15f * unit)
            )
        }
    }
}

/** Left, top and width of each bar on a 30 x 30 grid. */
private val LOGO_BARS = listOf(
    Triple(7f, 8.05f, 15f),
    Triple(7f, 13.85f, 11f),
    Triple(7f, 19.65f, 7f)
)
