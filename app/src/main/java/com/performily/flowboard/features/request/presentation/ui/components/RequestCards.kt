package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Divider
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType


@Composable
fun MyRequestCard(request: Request, onClick: () -> Unit, modifier: Modifier = Modifier) {
    RequestCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = request.requestTypeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            RequestStatusChip(request.status)
        }
        Text(
            text = RequestFormatters.shortPeriod(request.period),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = RequestFormatters.myRequestDetail(request),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


@Composable
fun ApprovalCard(
    request: Request,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onReject: () -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    RequestCard(modifier = modifier, onClick = onClick, spacing = 10.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RequestAvatar(name = request.requesterName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = RequestFormatters.shortName(request.requesterName),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = RequestFormatters.approvalSummary(request),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = RequestFormatters.sentAgo(request.submittedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                OutlinedButton(onClick = onReject, shape = RoundedCornerShape(8.dp)) { Text("Rechazar") }
                Button(onClick = onApprove, shape = RoundedCornerShape(8.dp)) { Text("Aprobar") }
            }
        }
    }
}

@Composable
fun RequestListRow(request: Request, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RequestAvatar(name = request.requesterName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = RequestFormatters.shortName(request.requesterName), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "${request.requestTypeName} · ${RequestFormatters.shortRange(request.period)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = RequestFormatters.approverLine(request),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RequestStatusChip(request.status)
        }
        HorizontalDivider(color = Divider)
    }
}


@Composable
fun RequestTypeRow(
    type: RequestType,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { menuExpanded = true }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RequestIconCircle(icon = FlowboardIcons.Article)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = type.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = RequestFormatters.typeCatalogDetail(type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            RequestChip(
                text = if (type.active) "Activo" else "Inactivo",
                containerColor = if (type.active) RequestColors.TypeActive else RequestColors.TypeInactive
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(FlowboardIcons.MoreVert, contentDescription = "Opciones de ${type.name}")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(if (type.active) "Desactivar" else "Activar") },
                        onClick = {
                            menuExpanded = false
                            onToggleStatus()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(RequestIcons.Delete, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
        HorizontalDivider(color = Divider)
    }
}
