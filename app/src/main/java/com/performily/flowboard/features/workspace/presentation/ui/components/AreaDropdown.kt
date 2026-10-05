package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.features.workspace.domain.entity.Area

@Composable
fun AreaDropdown(
    areas: List<Area>,
    selectedAreaId: Long?,
    onSelect: (Area) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    supportingText: String? = null
) {
    SelectField(
        label = "Área",
        options = areas,
        selected = areas.firstOrNull { it.id == selectedAreaId },
        optionLabel = { it.name },
        onSelect = onSelect,
        modifier = modifier,
        isError = isError,
        supportingText = supportingText
    )
}
