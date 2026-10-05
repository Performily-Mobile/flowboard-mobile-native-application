package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.performily.flowboard.core.designsystem.theme.AvatarContainer
import com.performily.flowboard.core.designsystem.theme.OnAvatarContainer

@Composable
fun EmployeeAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(AvatarContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = OnAvatarContainer,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (size > 48.dp) 20.sp else 14.sp
        )
    }
}
