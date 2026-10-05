package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.workspace.domain.entity.Area

@Composable
fun AreasTab(
    areas: List<Area>,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)) {
        items(areas, key = { it.id }) { area ->
            OrganizationListItem(
                icon = FlowboardIcons.AccountTree,
                title = area.name,
                subtitle = "${area.activeEmployees} colaboradores activos",
                trailing = {
                    if (!area.active) ActiveChip(active = false)
                }
            )
        }
    }
}
