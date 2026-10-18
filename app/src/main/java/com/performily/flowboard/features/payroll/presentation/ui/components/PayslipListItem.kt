package com.performily.flowboard.features.payroll.presentation.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performily.flowboard.core.designsystem.theme.AvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnAvatarContainer

/**
 * Tonal card of a payslip in the HR lists (MA-67, MA-68, MA-69): avatar with initials,
 * "Apellidos, Nombres", "Neto S/ …" and a status chip.
 *
 * @param name name of the employee as shown in lists.
 * @param initials initials shown in the avatar.
 * @param amount formatted net amount.
 * @param status status chip shown at the end of the card.
 * @param modifier modifier applied to the card.
 * @param onClick called when the card is tapped, or null when the card is not tappable.
 */
@Composable
fun PayslipListItem(
    name: String,
    initials: String,
    amount: String,
    status: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape = MaterialTheme.shapes.medium
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AvatarContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = initials, color = OnAvatarContainer, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = netAmountLabel(amount),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            status()
        }
    }
}
