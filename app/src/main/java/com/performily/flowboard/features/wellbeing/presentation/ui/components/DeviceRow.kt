package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.wellbeing.domain.entity.Device
import com.performily.flowboard.features.wellbeing.domain.valueobject.DeviceStatus

/**
 * Row of a device (MA-72, MA-73): code, measured metrics and status.
 *
 * @param device device to show.
 * @param modifier modifier applied to the row.
 * @param showLastReading true inside a space to show "última lectura hace 2 min"; false in the inventory.
 * @param onClick called when the row is tapped, or null when it is not clickable.
 */
@Composable
fun DeviceRow(
    device: Device,
    modifier: Modifier = Modifier,
    showLastReading: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = WellbeingIcons.Device,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = device.code, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle(device, showLastReading),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            WellbeingChip(
                text = when (device.status) {
                    DeviceStatus.LINKED -> "Vinculado"
                    DeviceStatus.IN_INVENTORY -> "En inventario"
                    DeviceStatus.INACTIVE -> "Inactivo"
                },
                containerColor = if (device.status == DeviceStatus.LINKED) {
                    WellbeingColors.Optimal
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
            )
        }
        HorizontalDivider(color = Divider.copy(alpha = 0.7f))
    }
}

/** Metrics summary, plus the last reading time when [showLastReading] is true. */
private fun subtitle(device: Device, showLastReading: Boolean): String {
    if (!showLastReading) return device.metricsSummary
    val lastReading = device.lastReadingAt
    return if (lastReading != null) {
        "${device.metricsSummary} · última lectura ${WellbeingFormatters.ago(lastReading)}"
    } else {
        "${device.metricsSummary} · sin lecturas"
    }
}
