package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Row with the name of a status, a bar with its share of the month and the number of days.
 *
 * @param label name of the status, for example "Puntuales".
 * @param count days of the month with that status.
 * @param total days of the month with any status; the bar shows count against it.
 * @param color color of the bar.
 * @param modifier modifier applied to the row.
 */
@Composable
internal fun MonthStatRow(
    label: String,
    count: Int,
    total: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.s),
        horizontalArrangement = Arrangement.spacedBy(Spacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(104.dp)
        )
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else count / total.toFloat() },
            modifier = Modifier
                .weight(1f)
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            strokeCap = StrokeCap.Round,
            gapSize = 0.dp,
            drawStopIndicator = {}
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End,
            modifier = Modifier.width(32.dp)
        )
    }
}
