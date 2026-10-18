package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.core.designsystem.component.FlowboardTabs
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType

/**
 * Pill selector for Temperatura / Iluminación / Aire (MA-74, MA-75), built on [FlowboardTabs].
 *
 * @param selected metric currently selected.
 * @param onSelect called with the metric the user taps.
 * @param modifier modifier applied to the selector.
 */
@Composable
fun MetricSelector(
    selected: MetricType,
    onSelect: (MetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowboardTabs(
        options = MetricType.entries,
        selected = selected,
        onSelect = onSelect,
        label = { it.shortLabel },
        modifier = modifier
    )
}
