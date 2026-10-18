package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.entity.RequestType


/**
 * Card of one of the viewer's own requests: type, status, period and a short detail.
 *
 * @param request request to display.
 * @param onClick called when the card is tapped.
 * @param modifier modifier applied to the card.
 */
@Composable
fun MyRequestCard(request: Request, onClick: () -> Unit, modifier: Modifier = Modifier) {
    RequestCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = request.requestTypeName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


/**
 * Card of a request waiting for the viewer's decision, with quick reject and approve buttons.
 *
 * @param request request to display.
 * @param isProcessing true while a decision on this request is being saved.
 * @param onClick called when the card is tapped.
 * @param onReject called when "Rechazar" is tapped.
 * @param onApprove called when "Aprobar" is tapped.
 * @param modifier modifier applied to the card.
 */
@Composable
fun ApprovalCard(
    request: Request,
    isProcessing: Boolean,
    onClick: () -> Unit,
    onReject: () -> Unit,
    onApprove: () -> Unit,
    modifier: Modifier = Modifier
) {
    RequestCard(modifier = modifier, onClick = onClick, spacing = Spacing.m) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.m), verticalAlignment = Alignment.CenterVertically) {
            RequestAvatar(name = request.requesterName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = RequestFormatters.shortName(request.requesterName),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = RequestFormatters.approvalSummary(request),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
            horizontalArrangement = Arrangement.spacedBy(Spacing.s, Alignment.End),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isProcessing) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                OutlinedButton(onClick = onReject) { Text("Rechazar") }
                Button(onClick = onApprove) { Text("Aprobar") }
            }
        }
    }
}

/**
 * Card of a request of any employee: requester, type, dates, approver and status.
 *
 * @param request request to display.
 * @param onClick called when the card is tapped.
 * @param modifier modifier applied to the card.
 */
@Composable
fun RequestListRow(request: Request, onClick: () -> Unit, modifier: Modifier = Modifier) {
    RequestCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RequestAvatar(name = request.requesterName)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = RequestFormatters.shortName(request.requesterName),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${request.requestTypeName} · ${RequestFormatters.shortRange(request.period)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = RequestFormatters.approverLine(request),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            RequestStatusChip(request.status)
        }
    }
}


/**
 * Card of a request type of the catalog, with its status and a menu to toggle or delete it.
 *
 * @param type request type to display.
 * @param onToggleStatus called when "Activar" or "Desactivar" is chosen.
 * @param onDelete called when "Eliminar" is chosen.
 * @param modifier modifier applied to the card.
 */
@Composable
fun RequestTypeRow(
    type: RequestType,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    RequestCard(modifier = modifier, onClick = { menuExpanded = true }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RequestIconCircle(icon = FlowboardIcons.Article)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = type.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = RequestFormatters.typeCatalogDetail(type),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
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
    }
}
