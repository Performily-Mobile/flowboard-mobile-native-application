package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.performily.flowboard.features.wellbeing.domain.valueobject.MetricType

/** Selector Temperatura / Iluminación / Aire (MA-74, MA-75). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricSelector(
    selected: MetricType,
    onSelect: (MetricType) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = MetricType.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        metrics.forEachIndexed { index, metric ->
            SegmentedButton(
                selected = metric == selected,
                onClick = { onSelect(metric) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = metrics.size)
            ) {
                Text(metric.shortLabel)
            }
        }
    }
}
