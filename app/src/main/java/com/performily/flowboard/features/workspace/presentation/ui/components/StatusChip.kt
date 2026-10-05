package com.performily.flowboard.features.workspace.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.theme.ActiveContainer
import com.performily.flowboard.core.designsystem.theme.OnSurface
import com.performily.flowboard.core.designsystem.theme.SuspendedContainer
import com.performily.flowboard.core.designsystem.theme.SurfaceContainerHigh
import com.performily.flowboard.core.designsystem.theme.TerminatedContainer
import com.performily.flowboard.features.workspace.domain.valueobject.EmploymentStatus

@Composable
fun StatusChip(
    status: EmploymentStatus,
    modifier: Modifier = Modifier
) {
    val container = when (status) {
        EmploymentStatus.ACTIVE -> ActiveContainer
        EmploymentStatus.SUSPENDED -> SuspendedContainer
        EmploymentStatus.TERMINATED -> TerminatedContainer
    }
    LabelChip(text = status.label(), containerColor = container, modifier = modifier)
}

@Composable
fun ActiveChip(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    LabelChip(
        text = if (active) "Activo" else "Inactivo",
        containerColor = if (active) ActiveContainer else SurfaceContainerHigh,
        modifier = modifier
    )
}

@Composable
fun LabelChip(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = OnSurface)
    }
}
