package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.component.FlowboardCard
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Secondary figure of an [AttendanceSummaryCard], such as "Tardanzas 3".
 *
 * @property label name of the figure.
 * @property value formatted value of the figure.
 */
data class SummaryStat(val label: String, val value: String)

/**
 * Summary of an attendance report: one main figure, an optional progress bar and a row of
 * secondary figures, all in one card.
 *
 * It replaces a grid of same-sized tiles, which gave every number the same weight and left an
 * uneven last row; here the key indicator leads and the rest support it.
 *
 * @param title name of the main figure.
 * @param value formatted main figure.
 * @param stats secondary figures shown in equal columns below a divider.
 * @param modifier modifier applied to the card.
 * @param caption optional short text next to the main figure.
 * @param progress optional value from 0 to 1 drawn as a bar under the main figure.
 */
@Composable
fun AttendanceSummaryCard(
    title: String,
    value: String,
    stats: List<SummaryStat>,
    modifier: Modifier = Modifier,
    caption: String? = null,
    progress: Float? = null
) {
    FlowboardCard(modifier = modifier, verticalSpacing = Spacing.m) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
                if (caption != null) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alignByBaseline()
                    )
                }
            }
        }
        if (progress != null) {
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        Row(modifier = Modifier.fillMaxWidth()) {
            stats.forEach { stat ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = stat.value, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
