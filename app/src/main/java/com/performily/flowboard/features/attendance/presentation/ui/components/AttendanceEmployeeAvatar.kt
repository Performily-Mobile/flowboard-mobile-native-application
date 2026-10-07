package com.performily.flowboard.features.attendance.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.AvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnAvatarContainer

@Composable
fun AttendanceEmployeeAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = name.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Box(
        modifier = modifier
            .size(40.dp)
            .background(AvatarContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(initials.ifBlank { "?" }, color = OnAvatarContainer, style = MaterialTheme.typography.labelLarge)
    }
}
