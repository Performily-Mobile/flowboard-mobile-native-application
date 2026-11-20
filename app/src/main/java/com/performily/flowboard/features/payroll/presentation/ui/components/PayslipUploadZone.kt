package com.performily.flowboard.features.payroll.presentation.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.performily.flowboard.core.designsystem.component.FlowboardCard
import com.performily.flowboard.core.designsystem.component.FlowboardLeadingIcon
import com.performily.flowboard.core.designsystem.icon.FlowboardIcons
import com.performily.flowboard.core.designsystem.theme.Spacing
import com.performily.flowboard.features.payroll.presentation.state.SelectedPayslipFile

/**
 * Tonal card used to pick the payslip files exported by the payroll system (MA-67).
 *
 * It reads as a list row with a clear action instead of a dashed web drop zone, which has no
 * meaning on a phone. Opens the system document picker and allows selecting several files at once.
 * Any file type can be picked: the PDF-only validation is done by the domain and shown in the
 * error banner.
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
    FlowboardCard(
        modifier = modifier.alpha(if (enabled) 1f else 0.6f),
        onClick = if (enabled) {
            { picker.launch(arrayOf("*/*")) }
        } else {
            null
        },
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowboardLeadingIcon(
                icon = FlowboardIcons.Upload,
                containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                contentColor = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Subir boletas en PDF",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Icon(
                imageVector = FlowboardIcons.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
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
