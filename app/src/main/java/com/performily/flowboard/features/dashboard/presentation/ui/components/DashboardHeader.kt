package com.performily.flowboard.features.dashboard.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.performily.flowboard.core.designsystem.theme.Spacing

/**
 * Header of the dashboards: the greeting as a large title with the notifications bell.
 *
 * The greeting stands alone on one level with room around it; the "updated" time lives at the end
 * of the dashboard, where it does not compete with the greeting.
 *
 * @param greeting greeting, such as "Buenas noches, Patricia".
 * @param modifier modifier applied to the header.
 * @param actions trailing actions, such as the notifications bell.
 */
@Composable
internal fun DashboardHeader(
    greeting: String,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                .padding(start = Spacing.screenHorizontal, end = Spacing.s, top = Spacing.l, bottom = Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = greeting,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            actions()
        }
    }
}

/**
 * Small centered line at the end of a dashboard with the time of the last update.
 *
 * @param text line to show, such as "Actualizado hace 6 min".
 * @param modifier modifier applied to the line.
 */
@Composable
internal fun DashboardUpdatedFooter(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.s),
        textAlign = TextAlign.Center
    )
}
