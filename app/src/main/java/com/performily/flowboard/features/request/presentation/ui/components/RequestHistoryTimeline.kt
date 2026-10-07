package com.performily.flowboard.features.request.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.features.request.domain.entity.Request
import com.performily.flowboard.features.request.domain.valueobject.ApproverType
import com.performily.flowboard.features.request.domain.valueobject.RequestStatus


data class TimelineItem(
    val title: String,
    val detail: String,
    val isCurrent: Boolean
)

@Composable
fun RequestHistoryTimeline(items: List<TimelineItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items.forEach { item ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .size(12.dp)
                        .background(
                            if (item.isCurrent) RequestColors.TimelineCurrent else RequestColors.TimelinePast,
                            CircleShape
                        )
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = item.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                    Text(
                        text = item.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}


internal fun timelineOf(request: Request, viewerId: Long): List<TimelineItem> {
    val items = mutableListOf<TimelineItem>()
    fun who(actorId: Long?, actorName: String?): String = when {
        actorId == null -> "sistema"
        actorId == viewerId -> "por ti"
        else -> actorName?.let(RequestFormatters::shortName) ?: "Colaborador #$actorId"
    }

    request.history.forEach { entry ->
        val time = RequestFormatters.shortDateTime(entry.occurredAt)
        val actor = who(entry.actorId, entry.actorName)
        when {
            entry.previousStatus == null -> {
                items += TimelineItem("Solicitud enviada", "$time · $actor", isCurrent = false)
                if (request.history.size == 1) {
                    val assigned = if (request.approverType == ApproverType.HR_STAFF) {
                        TimelineItem("Asignada a Recursos Humanos", "$time · sin jefe directo", isCurrent = false)
                    } else {
                        TimelineItem("Asignada a ${RequestFormatters.shortName(request.approverName)}", "$time · jefe directo", isCurrent = false)
                    }
                    items += assigned
                }
            }
            entry.newStatus == RequestStatus.UNDER_REVIEW ->
                items += TimelineItem("Devuelta a revisión", "$time · $actor", isCurrent = false)
            entry.newStatus == RequestStatus.IN_PROGRESS ->
                items += TimelineItem("Solicitud reenviada", "$time · $actor", isCurrent = false)
            entry.newStatus == RequestStatus.APPROVED ->
                items += TimelineItem("Aprobada", "$time · $actor", isCurrent = false)
            entry.newStatus == RequestStatus.REJECTED ->
                items += TimelineItem(
                    "Rechazada",
                    listOfNotNull("$time · $actor", entry.comment?.let { "Motivo: $it" }).joinToString("\n"),
                    isCurrent = false
                )
            entry.newStatus == RequestStatus.CANCELLED ->
                items += TimelineItem("Cancelada", "$time · $actor", isCurrent = false)
        }
    }

    if (request.status == RequestStatus.IN_PROGRESS) {
        items += TimelineItem("Pendiente", "Esperando la decisión del aprobador", isCurrent = true)
    } else if (items.isNotEmpty()) {
        items[items.lastIndex] = items.last().copy(isCurrent = true)
    }
    return items
}
