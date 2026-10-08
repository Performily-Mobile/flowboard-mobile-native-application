package com.performily.flowboard.features.request.presentation.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Outline
import com.performily.flowboard.core.domain.FileReference
import com.performily.flowboard.features.request.domain.valueobject.AttachmentUpload


data class PickedFile(
    val uri: String,
    val name: String,
    val contentType: String,
    val sizeInBytes: Long
)

@Composable
fun rememberAttachmentPicker(onPicked: (PickedFile) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { context.describeFile(it) }?.let(onPicked)
    }
    return { launcher.launch(AttachmentUpload.ALLOWED_CONTENT_TYPES.toTypedArray()) }
}

@Composable
fun AttachmentZone(
    required: Boolean,
    isUploading: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null
) {
    val borderColor = if (error != null) MaterialTheme.colorScheme.error else Outline
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(RequestColors.AttachmentZone, RoundedCornerShape(12.dp))
                .drawBehind {
                    drawRoundRect(
                        color = borderColor,
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)),
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                }
                .clickable(enabled = !isUploading, onClick = onClick)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isUploading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                Icon(FlowboardIcons.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = if (required) "Adjuntar sustento" else "Adjuntar sustento (opcional)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "PDF, JPG o PNG · máx. 5 MB",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        error?.let {
            Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun AttachmentRow(
    file: FileReference,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    RequestCard(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RequestIconCircle(
                icon = RequestIcons.Description,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = file.fileName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    text = RequestFormatters.fileDetail(file),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onRemove != null) {
                IconButton(onClick = onRemove) {
                    Icon(FlowboardIcons.Close, contentDescription = "Quitar adjunto")
                }
            }
        }
    }
}

private fun Context.describeFile(uri: Uri): PickedFile {
    val contentType = contentResolver.getType(uri) ?: "application/octet-stream"
    var name: String? = null
    var size = 0L
    contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
        ?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) name = cursor.getString(nameIndex)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
            }
        }
    return PickedFile(
        uri = uri.toString(),
        name = name ?: uri.lastPathSegment ?: "sustento",
        contentType = contentType,
        sizeInBytes = size
    )
}
