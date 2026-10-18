package com.performily.flowboard.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radii shared by every component of the app.
 *
 * Material components read these values, so chips, cards, text fields, dialogs and sheets keep the
 * same rounded look everywhere:
 * extraSmall for text fields and menus, small (a full pill) for chips, medium for cards,
 * large for floating buttons and extraLarge for dialogs and sheets.
 */
val FlowboardShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(percent = 50),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
