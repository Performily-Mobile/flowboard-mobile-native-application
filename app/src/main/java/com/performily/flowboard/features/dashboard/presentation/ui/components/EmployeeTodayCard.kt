package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.dashboard.domain.entity.TodayAttendance

/**
 * Highlighted card with the attendance of the day and a button that opens the Attendance tab.
 *
 * The title and button change with the state of the day: before the check-in, while working, and
 * once the day is complete.
 *
 * @param today attendance of the day, or null when it could not be loaded.
 * @param onClick called when the button is tapped.
 * @param modifier modifier applied to the card.
 */
@Composable
internal fun EmployeeTodayCard(
    today: TodayAttendance?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = EmployeeDashboardFormatters.todayCard(today)
    val status = today?.record?.status
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hoy" + (today?.let { " · ${EmployeeDashboardFormatters.longDay(it.date)}" } ?: ""),
                    style = MaterialTheme.typography.labelLarge
                )
                status?.let {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface) {
                        Text(
                            text = it.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            Text(text = text.title, style = MaterialTheme.typography.titleLarge)
            Text(text = text.detail, style = MaterialTheme.typography.bodyMedium)
            Button(
                onClick = onClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.s)
                    .height(52.dp)
            ) {
                Text(text.action)
            }
        }
    }
}
