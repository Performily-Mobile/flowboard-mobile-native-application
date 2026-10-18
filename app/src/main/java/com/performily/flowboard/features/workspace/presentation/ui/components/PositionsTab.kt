package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.workspace.domain.entity.Position

/**
 * List of the positions of the organization.
 *
 * @param positions positions to show.
 * @param modifier modifier applied to the list.
 * @param contentPadding padding of the list content; the screen adds the room for its collapsing header.
 */
@Composable
fun PositionsTab(
    positions: List<Position>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = Spacing.screenHorizontal,
        end = Spacing.screenHorizontal,
        top = 8.dp,
        bottom = Spacing.fabClearance
    )
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Spacing.itemGap)
    ) {
        items(positions, key = { it.id }) { position ->
            OrganizationListItem(
                icon = FlowboardIcons.Person,
                title = position.title,
                subtitle = "${position.areaName} · Sueldo ref. ${position.referenceSalary.toDisplay()}",
                trailing = { ActiveChip(active = position.active) }
            )
        }
    }
}
