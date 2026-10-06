package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.features.workspace.domain.entity.Area

@Composable
fun AreasTab(
    areas: List<Area>,
    onDeactivateClick: (Area) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp)) {
        items(areas, key = { it.id }) { area ->
            OrganizationListItem(
                icon = FlowboardIcons.AccountTree,
                title = area.name,
                subtitle = "${area.activeEmployees} colaboradores activos",
                trailing = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LabelChip(
                            text = if (area.active) "Activa" else "Inactiva",
                            containerColor = if (area.active) ActiveContainer else SurfaceContainerHigh
                        )
                        if (area.active) {
                            AreaMenu(onDeactivateClick = { onDeactivateClick(area) })
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun AreaMenu(onDeactivateClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(FlowboardIcons.MoreVert, contentDescription = "Opciones del área")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Desactivar área") },
                onClick = {
                    expanded = false
                    onDeactivateClick()
                }
            )
        }
    }
}
