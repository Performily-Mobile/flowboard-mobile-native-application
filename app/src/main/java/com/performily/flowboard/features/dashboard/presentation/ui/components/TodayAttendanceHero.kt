package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.component.animatedNumber
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.dashboard.domain.entity.AreaAttendance
import kotlin.math.roundToInt

/**
 * Highlighted card at the top of the HR dashboard with today's attendance.
 *
 * It answers the first question of the day, "how is attendance going today?", with one large
 * figure, a progress bar and a short context line, using the primary container so it stands out
 * from the indicator cards below.
 *
 * @param attendance attendance of each area today, or null when it could not be loaded.
 * @param onClick called when the card is tapped, to open the attendance by area.
 * @param modifier modifier applied to the card.
 */
@Composable
internal fun TodayAttendanceHero(
    attendance: List<AreaAttendance>?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val average = attendance?.takeIf { it.isNotEmpty() }?.map { it.percentage }?.average()?.roundToInt()
    val content = MaterialTheme.colorScheme.onPrimaryContainer
    val animatedProgress by animateFloatAsState(
        targetValue = (average ?: 0) / 100f,
        animationSpec = tween(durationMillis = 700),
        label = "todayAttendance"
    )
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = content
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.xl, vertical = Spacing.l + Spacing.xs),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(FlowboardIcons.Schedule, contentDescription = null, modifier = Modifier.padding(end = Spacing.s))
                Text(
                    text = "Asistencia de hoy",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                Icon(FlowboardIcons.ChevronRight, contentDescription = null)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(Spacing.s)) {
                Text(
                    text = average?.let { "${animatedNumber(it.toString())}%" } ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = "marcó entrada",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.alignByBaseline()
                )
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.7f),
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )
            Text(
                text = heroCaption(attendance),
                style = MaterialTheme.typography.bodyMedium,
                color = content.copy(alpha = 0.8f)
            )
        }
    }
}

/**
 * Context line of the hero card.
 *
 * @param attendance attendance of each area today, or null when it could not be loaded.
 * @return a short sentence that explains the figure.
 */
private fun heroCaption(attendance: List<AreaAttendance>?): String = when {
    attendance == null -> "No se pudo cargar la asistencia de hoy."
    attendance.isEmpty() -> "Todavía no hay áreas con colaboradores activos."
    attendance.all { it.percentage == 0 } -> "Nadie ha marcado entrada todavía en las ${attendance.size} áreas."
    else -> {
        val best = attendance.maxBy { it.percentage }
        val waiting = attendance.count { it.percentage == 0 }
        val lead = "Promedio de ${attendance.size} áreas. ${best.areaName} lidera con ${best.percentage}%"
        when (waiting) {
            0 -> "$lead."
            1 -> "$lead y 1 área aún no marca."
            else -> "$lead y $waiting áreas aún no marcan."
        }
    }
}
