package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Position

@Composable
fun PositionsTab(
    positions: List<Position>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)) {
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
