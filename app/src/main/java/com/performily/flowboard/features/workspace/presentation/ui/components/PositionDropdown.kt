package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.features.workspace.domain.entity.Position

@Composable
fun PositionDropdown(
    positions: List<Position>,
    selectedPositionId: Long?,
    onSelect: (Position) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: String? = null
) {
    SelectField(
        label = "Posición",
        options = positions,
        selected = positions.firstOrNull { it.id == selectedPositionId },
        optionLabel = { it.title },
        onSelect = onSelect,
        modifier = modifier,
        enabled = enabled,
        isError = isError,
        supportingText = supportingText
    )
}
