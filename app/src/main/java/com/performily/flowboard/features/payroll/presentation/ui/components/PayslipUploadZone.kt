package com.performily.flowboard.features.payroll.presentation.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.features.payroll.presentation.state.SelectedPayslipFile

/**
 * Dashed card used to pick the payslip files exported by the payroll system (MA-67).
 *
 * Opens the system document picker and allows selecting several files at once. Any file type
 * can be picked: the PDF-only validation is done by the domain and shown in the error banner.
 *
 * @param enabled whether the card can be tapped
 * @param supportingText helper text shown under the title, for example the upload progress
 * @param onFilesSelected called with the description of every picked file
 * @param modifier modifier applied to the card
 */
@Composable
fun PayslipUploadZone(
    enabled: Boolean,
    supportingText: String,
    onFilesSelected: (List<SelectedPayslipFile>) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val files = uris.map { context.describeFile(it) }
        if (files.isNotEmpty()) onFilesSelected(files)
    }
    val outline = MaterialTheme.colorScheme.outline
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawRoundRect(
                    color = outline,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()))
                    ),
                    cornerRadius = CornerRadius(12.dp.toPx() - strokeWidth / 2)
                )
            }
            .clickable(enabled = enabled) { picker.launch(arrayOf("*/*")) }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = FlowboardIcons.Upload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = "Seleccionar archivos del sistema de planilla",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = supportingText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Describes a file picked in the system document picker.
 *
 * When the provider does not report a size, it is read directly from the file descriptor.
 * When the provider reports a generic content type for a file named ".pdf", the type is
 * normalized to PDF.
 *
 * @receiver context used to query the content resolver
 * @param uri content URI of the picked file
 * @return the name, content type and size of the file
 */
private fun Context.describeFile(uri: Uri): SelectedPayslipFile {
    val providerType = contentResolver.getType(uri)
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
    if (size <= 0L) {
        size = runCatching {
            contentResolver.openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L
        }.getOrDefault(0L).coerceAtLeast(0L)
    }
    val fileName = name ?: uri.lastPathSegment ?: "archivo"
    val contentType = if (
        (providerType == null || providerType == "application/octet-stream") &&
        fileName.endsWith(".pdf", ignoreCase = true)
    ) {
        "application/pdf"
    } else {
        providerType ?: "application/octet-stream"
    }
    return SelectedPayslipFile(
        uri = uri.toString(),
        name = fileName,
        contentType = contentType,
        sizeInBytes = size
    )
}
