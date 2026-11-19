package com.performily.flowboard.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.StatusTone
import com.performily.flowboard.core.designsystem.theme.statusColors

/**
 * Pill that tells the status of an item, such as "Pendiente", "Aprobada" or "Inasistencia".
 *
 * The color comes from the [tone] and the theme, so the same meaning always looks the same.
 * Show a badge only when the status adds information: a list already filtered by one status,
 * or where nearly every item is active, should show badges only on the exceptions.
 *
 * @param text label of the status.
 * @param tone meaning of the status.
 * @param modifier modifier applied to the badge.
 */
@Composable
fun FlowboardStatusBadge(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.statusColors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = colors.content(tone),
        maxLines = 1,
        modifier = modifier
            .background(colors.container(tone), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
