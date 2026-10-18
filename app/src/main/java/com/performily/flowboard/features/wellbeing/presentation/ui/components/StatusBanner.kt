package com.performily.flowboard.features.wellbeing.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons

/** Tone of a [StatusBanner]: a dangerous condition or outdated information. */
enum class BannerTone { DANGER, WARNING }

/**
 * Highlighted notice: dangerous condition (MA-72) or outdated information (MA-81, MA-74).
 *
 * @param message text of the notice.
 * @param tone tone that sets the colors.
 * @param modifier modifier applied to the banner.
 */
@Composable
fun StatusBanner(
    message: String,
    tone: BannerTone,
    modifier: Modifier = Modifier
) {
    val container = if (tone == BannerTone.DANGER) WellbeingColors.DangerBanner else WellbeingColors.StaleBanner
    val content = if (tone == BannerTone.DANGER) WellbeingColors.OnDangerBanner else WellbeingColors.OnStaleBanner
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(container, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(FlowboardIcons.Warning, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = content)
    }
}
